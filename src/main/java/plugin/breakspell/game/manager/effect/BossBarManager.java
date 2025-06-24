package plugin.breakspell.game.manager.effect;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * ボスバーを作成・更新・削除するクラス。
 */
public class BossBarManager {

  private BossBar bossBar;
  private static final String BAR_TITLE = "残り時間 : ";
  private static final double FULL_PROGRESS = 1.0;

  /**
   * ボスバーを作成する。
   */
  public void createBossBar() {
    bossBar = Bukkit.createBossBar(BAR_TITLE, BarColor.GREEN, BarStyle.SOLID);
    bossBar.setProgress(FULL_PROGRESS);
  }

  /**
   * ボスバーをプレイヤーの画面上に設定する。
   *
   * @param player プレイヤー
   */
  public void showBossBarToPlayer(Player player) {
    bossBar.addPlayer(player);
  }

  /**
   * ボスバーの情報を更新する。残り時間に応じてバーの色を変える。
   *
   * @param timeLeft        残り時間
   * @param initialGameTime 初期設定時間
   */
  public void updateBossBar(int timeLeft, int initialGameTime) {
    bossBar.setProgress(Math.min(FULL_PROGRESS, (double) timeLeft / initialGameTime));
    bossBar.setTitle(BAR_TITLE + timeLeft);

    BarColor color = BarColor.GREEN;
    if (timeLeft <= 5) {
      color = BarColor.RED;
    } else if (timeLeft <= initialGameTime / 3) {
      color = BarColor.PINK;
    } else if (timeLeft <= initialGameTime / 2) {
      color = BarColor.YELLOW;
    }
    bossBar.setColor(color);
  }

  /**
   * ボスバーを削除する。
   */
  public void removeBossBar() {
    bossBar.removeAll();
    bossBar = null;
  }
}
