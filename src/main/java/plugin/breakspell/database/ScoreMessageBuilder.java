package plugin.breakspell.database;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import plugin.breakspell.database.data.PlayerScore;
import plugin.breakspell.game.constant.GameDifficulty;

/**
 * DBから取得したスコアリストをメッセージ表示用に整形するクラス。<br>
 * 新着リスト、全体ランキングリスト、難易度別ランキングリストとして整形する。
 */
public class ScoreMessageBuilder {

  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
  private static final String BORDER_LINE = "-----------------------------------------------------";
  private static final String FOOTER_LINE = "=====================================================";

  public static List<String> buildNewlyScoreList(List<PlayerScore> playerScoreList) {
    List<String> scoreLines = new ArrayList<>();
    scoreLines.add(
        ChatColor.GOLD + "================ ◆ スコア履歴 (新着 " + playerScoreList.size() + " 件) ◆ ================");
    scoreLines.add(" No |    プレイヤー名    | スコア |  難易度  |        プレイ日時");
    scoreLines.add(BORDER_LINE);

    for (PlayerScore playerScore : playerScoreList) {
      scoreLines.add(
          String.format(" %2d | %17s | %5d | %7s | %20s ",
              playerScore.getId(),
              playerScore.getPlayerName(),
              playerScore.getScore(),
              playerScore.getDifficulty(),
              playerScore.getRegisteredAt()
                  .format(FORMATTER))
      );
    }
    scoreLines.add(ChatColor.GOLD + FOOTER_LINE);
    return scoreLines;
  }

  public static List<String> buildRankedScoreList(List<PlayerScore> playerScoreList) {
    List<String> scoreLines = new ArrayList<>();
    scoreLines.add(ChatColor.GOLD + "================= ◆ 全体ランキングTOP5 ◆ =================");
    scoreLines.add(" 順位 |    プレイヤー名    | スコア |  難易度  |        プレイ日時");
    scoreLines.add(BORDER_LINE);
    int rank = 1;
    for (PlayerScore playerScore : playerScoreList) {
      scoreLines.add(
          String.format(" %2d位 | %17s | %5d | %7s | %20s ",
              rank,
              playerScore.getPlayerName(),
              playerScore.getScore(),
              playerScore.getDifficulty(),
              playerScore.getRegisteredAt()
                  .format(FORMATTER))
      );
      rank++;
    }
    scoreLines.add(ChatColor.GOLD + FOOTER_LINE);
    return scoreLines;
  }

  public static List<String> buildRankedByDifficultyScoreList(
      List<PlayerScore> playerScoreList, GameDifficulty gameDifficulty) {

    String difficultyLabel = String.format("%4s", gameDifficulty.getLabel());

    List<String> scoreLines = new ArrayList<>();
    scoreLines.add(ChatColor.GOLD + "=========== ◆ 難易度別ランキング (" + difficultyLabel + ") TOP5 ◆ ===========");
    scoreLines.add(" 順位 |    プレイヤー名    | スコア |             プレイ日時");
    scoreLines.add(BORDER_LINE);
    int rank = 1;
    for (PlayerScore playerScore : playerScoreList) {
      scoreLines.add(
          String.format(" %2d位 | %17s | %5d | %25s ",
              rank,
              playerScore.getPlayerName(),
              playerScore.getScore(),
              playerScore.getRegisteredAt()
                  .format(FORMATTER))
      );
      rank++;
    }
    scoreLines.add(ChatColor.GOLD + FOOTER_LINE);
    return scoreLines;
  }
}
