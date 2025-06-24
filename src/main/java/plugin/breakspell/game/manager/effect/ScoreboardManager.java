package plugin.breakspell.game.manager.effect;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import plugin.breakspell.game.data.PlayerGameData;

/**
 * スコアボードの作成・更新・削除を担うクラス。
 */
public class ScoreboardManager {

  private static final String CURRENT_SCORE = "currentScore";
  private static final String SCOREBOARD_NAME =
      ChatColor.DARK_AQUA + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "◆現在のスコア◆";
  private static final String LABEL_COMBO = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "コンボ";
  private static final String LABEL_PAIR = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "ペア";
  private static final String LABEL_SCORE = ChatColor.GOLD + "" + ChatColor.BOLD + "スコア";

  /**
   * スコアボードを作成する。
   *
   * @param player 　ゲームを実行中のプレイヤー
   */
  public void createScoreboard(Player player) {
    Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
    Objective objective = scoreboard.registerNewObjective(
        CURRENT_SCORE,
        Criteria.DUMMY,
        SCOREBOARD_NAME);
    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    objective.getScore(LABEL_COMBO).setScore(0);
    objective.getScore(LABEL_PAIR).setScore(0);
    objective.getScore(LABEL_SCORE).setScore(0);

    player.setScoreboard(scoreboard);
  }

  /**
   * スコアボードを更新する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   */
  public void updateCurrentScore(Player player, PlayerGameData playerGameData) {
    Scoreboard scoreboard = player.getScoreboard();
    Objective objective = scoreboard.getObjective(CURRENT_SCORE);

    if (objective != null) {
      objective.getScore(LABEL_COMBO).setScore(playerGameData.getConsecutivePairs());
      objective.getScore(LABEL_PAIR).setScore(playerGameData.getMatchedPair());
      objective.getScore(LABEL_SCORE).setScore(playerGameData.getScore());
    }
  }

  /**
   * スコアボードを削除する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void clearScoreboard(Player player) {
    player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
  }
}
