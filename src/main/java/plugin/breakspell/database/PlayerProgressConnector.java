package plugin.breakspell.database;

import java.util.UUID;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import plugin.breakspell.database.data.PlayerProgress;
import plugin.breakspell.database.mapper.PlayerProgressMapper;
import plugin.breakspell.game.constant.GameDifficulty;

/**
 * DBと接続してゲームの進捗状況に関する処理を実行するクラス。
 */
public class PlayerProgressConnector {

  private final SqlSessionFactory sqlSessionFactory;

  public PlayerProgressConnector(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }

  /**
   * いずれかの難易度でゲームをプレイしたことがあるか、DBの進捗状況テーブルを確認する。
   *
   * @param playerUuid プレイヤーのUUID
   * @return ゲームのプレイ有無（反転）
   */
  public boolean hasNotPlayedAnyDifficulty(UUID playerUuid) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerProgressMapper mapper = session.getMapper(PlayerProgressMapper.class);
      return !mapper.hasPlayedAnyDifficulty(playerUuid.toString());
    }
  }

  /**
   * 指定の難易度でゲームをプレイしたことがあるか、DBの進捗状況テーブルを確認する。
   *
   * @param playerUuid     プレイヤーのUUID
   * @param gameDifficulty ゲームの難易度
   * @return ゲームのプレイ有無（反転）
   */
  public boolean hasNotPlayed(UUID playerUuid, GameDifficulty gameDifficulty) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerProgressMapper mapper = session.getMapper(PlayerProgressMapper.class);
      Boolean hasPlayed = mapper.hasPlayed(playerUuid.toString(), gameDifficulty.toString());
      return hasPlayed == null || !hasPlayed;
    }
  }

  /**
   * いずれかの難易度でゲームをクリアしたことがあるか、DBの進捗状況テーブルを確認する。
   *
   * @param playerUuid プレイヤーのUUID
   * @return ゲームのクリア有無（反転）
   */
  public boolean hasNotClearedAnyDifficulty(UUID playerUuid) {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PlayerProgressMapper mapper = session.getMapper(PlayerProgressMapper.class);
      return !mapper.hasClearedAnyDifficulty(playerUuid.toString());
    }
  }

  /**
   * 指定の難易度で初めてゲームをプレイした際に、DBの進捗状況テーブルにゲームの進捗状況を登録する。
   *
   * @param playerUuid     プレイヤーのUUID
   * @param gameDifficulty ゲームの難易度
   */
  public void insertGameProgress(UUID playerUuid, GameDifficulty gameDifficulty) {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      PlayerProgressMapper mapper = session.getMapper(PlayerProgressMapper.class);
      Boolean isPlayed = mapper.hasPlayed(playerUuid.toString(), gameDifficulty.getInputArg());
      if (isPlayed == null || !isPlayed) {
        mapper.insertPlayerProgress(
            new PlayerProgress(
                playerUuid.toString(),
                gameDifficulty.getInputArg(),
                true,
                false,
                null));
      }
    }
  }

  /**
   * 指定の難易度で初めてゲームをクリアした際に、DBの進捗状況テーブルのクリア状況を更新する。
   *
   * @param playerUuid     プレイヤーのUUID
   * @param gameDifficulty ゲームの難易度
   */
  public void updateGameClear(UUID playerUuid, GameDifficulty gameDifficulty) {
    try (SqlSession session = sqlSessionFactory.openSession(true)) {
      PlayerProgressMapper mapper = session.getMapper(PlayerProgressMapper.class);
      if (!mapper.hasCleared(playerUuid.toString(), gameDifficulty.getInputArg())) {
        mapper.updatePlayerProgress(playerUuid.toString(), gameDifficulty.getInputArg());
      }
    }
  }
}
