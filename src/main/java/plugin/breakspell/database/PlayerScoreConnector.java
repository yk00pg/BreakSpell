package plugin.breakspell.database;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bukkit.entity.Player;
import plugin.breakspell.database.data.PlayerScore;
import plugin.breakspell.database.mapper.PlayerScoreMapper;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.data.PlayerGameData;
import plugin.breakspell.game.manager.effect.SendTextManager;

/**
 * DBと接続してゲームのスコア情報に関する処理を実行するクラス。
 */
public class PlayerScoreConnector {

  private final SqlSessionFactory sqlSessionFactory;
  private final SendTextManager sendTextManager;

  public PlayerScoreConnector(
      SqlSessionFactory sqlSessionFactory, SendTextManager sendTextManager) {

    this.sqlSessionFactory = sqlSessionFactory;
    this.sendTextManager = sendTextManager;
  }

  /**
   * DBからスコアリストを取得し、メッセージとして表示する（新着リスト）。
   *
   * @param player コマンドを実行またはスコアメニューをクリックしたプレイヤー
   */
  public boolean showNewlyScoreList(Player player) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerScoreMapper mapper = session.getMapper(PlayerScoreMapper.class);
      List<PlayerScore> playerScoreList = mapper.selectNewlyScoreList();

      if (playerScoreList.isEmpty()) {
        sendTextManager.sendNotExitsScoreListMessage(player);
        return false;
      } else {
        List<String> scoreLines = ScoreMessageBuilder.buildNewlyScoreList(playerScoreList);
        sendTextManager.sendScoreLines(player, scoreLines);
        return true;
      }
    }
  }

  /**
   * DBからスコアリストを取得し、メッセージとして表示する（全体ランキングリスト）。
   *
   * @param player コマンドを実行またはスコアメニューをクリックしたプレイヤー
   */
  public boolean showRankedScoreList(Player player) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerScoreMapper mapper = session.getMapper(PlayerScoreMapper.class);
      List<PlayerScore> playerScoreList = mapper.selectRankedScoreList();

      if (playerScoreList.isEmpty()) {
        sendTextManager.sendNotExitsScoreListMessage(player);
        return false;
      } else {
        List<String> scoreLines = ScoreMessageBuilder.buildRankedScoreList(playerScoreList);
        sendTextManager.sendScoreLines(player, scoreLines);
        return true;
      }
    }
  }

  /**
   * DBからスコアリストを取得し、メッセージとして表示する（難易度別ランキングリスト）。
   *
   * @param player         コマンドを実行またはスコアメニューをクリックしたプレイヤー
   * @param gameDifficulty ゲームの難易度
   */
  public boolean showRankedByDifficultyScoreList(Player player, GameDifficulty gameDifficulty) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerScoreMapper mapper = session.getMapper(PlayerScoreMapper.class);
      List<PlayerScore> playerScoreList =
          mapper.selectRankedByDifficultyScoreList(gameDifficulty.toString());

      if (playerScoreList.isEmpty()) {
        sendTextManager.sendNotExitsScoreListMessage(player);
        return false;
      } else {
        List<String> scoreLines =
            ScoreMessageBuilder.buildRankedByDifficultyScoreList(playerScoreList, gameDifficulty);
        sendTextManager.sendScoreLines(player, scoreLines);
        return true;
      }
    }
  }

  /**
   * DBから指定の難易度のハイスコアを取得し、存在しない場合は0を返す。
   *
   * @param gameDifficulty ゲームの難易度
   * @return 指定の難易度のハイスコア
   */
  public int getHighScore(GameDifficulty gameDifficulty) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerScoreMapper mapper = session.getMapper(PlayerScoreMapper.class);
      return Optional.ofNullable(
              mapper.selectHighScore(gameDifficulty.getInputArg()))
          .orElse(0);
    }
  }

  /**
   * DBにプレイヤーのスコア情報を登録する。
   *
   * @param playerGameData プレイヤーデータ
   * @param gameDifficulty ゲームの難易度
   */
  public void insertNewScore(PlayerGameData playerGameData, GameDifficulty gameDifficulty) {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      PlayerScoreMapper mapper = session.getMapper(PlayerScoreMapper.class);
      mapper.insertNewScore(
          new PlayerScore(
              playerGameData.getPlayerUuid().toString(),
              playerGameData.getPlayerName(),
              playerGameData.getScore(),
              gameDifficulty.getInputArg()));
    }
  }
}
