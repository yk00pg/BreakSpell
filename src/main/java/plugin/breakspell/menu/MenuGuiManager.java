package plugin.breakspell.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import plugin.breakspell.Main;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.TickTime;
import plugin.breakspell.game.manager.execution.GameStatusChecker;

/**
 * ゲームメニューGUIを管理するクラス
 */
public class MenuGuiManager {

  private final Main main;
  private final GameStatusChecker gameStatusChecker;

  public MenuGuiManager(Main main, GameStatusChecker gameStatusChecker) {
    this.main = main;
    this.gameStatusChecker = gameStatusChecker;
  }

  /**
   * ゲームメニューGUIを開く。
   *
   * @param player コマンドを実行したプレイヤー
   */
  public void openGameMenuGui(Player player) {
    Inventory gameMenuGui = Bukkit.createInventory(null, 9, MenuGui.GAME_MENU);

    setGameMenu(player, gameMenuGui);
    player.openInventory(gameMenuGui);
  }

  /**
   * ゲームメニューを設置する。
   *
   * @param player      コマンドを実行したプレイヤー
   * @param gameMenuGui ゲームメニューGUI
   */
  private void setGameMenu(Player player, Inventory gameMenuGui) {
    for (GameMenu menu : GameMenu.values()) {

      boolean isDisabled = switch (menu) {
        case PROLOGUE -> gameStatusChecker.isFirstPlay(player);
        case NORMAL -> gameStatusChecker.hasNotPlayedPreDifficulty(player, GameDifficulty.NORMAL);
        case HARD -> gameStatusChecker.hasNotPlayedPreDifficulty(player, GameDifficulty.HARD);
        case EPILOGUE -> gameStatusChecker.isFirstClear(player);
        default -> false;
      };

      gameMenuGui.setItem(menu.getSlotNum(), menu.getItemStack(isDisabled));
    }
  }

  /**
   * ゲームメニューGUIを2秒後に再び開く。
   *
   * @param player ゲームメニューGUIを開いていたプレイヤー
   */
  public void reopenGameMenuGui(Player player) {
    Bukkit.getScheduler().runTaskLater(main, () -> openGameMenuGui(player),
        TickTime.DELAY_2_SECONDS);
  }

  /**
   * スコアメニューGUIを開く。
   *
   * @param player コマンドを実行したプレイヤー
   */
  public void openScoreMenuGui(Player player) {
    Inventory scoreMenuGui = Bukkit.createInventory(null, 9, MenuGui.SCORE_MENU);

    for (ScoreMenu menu : ScoreMenu.values()) {
      scoreMenuGui.setItem(menu.getSlotNum(), menu.getItemStack());
    }

    player.openInventory(scoreMenuGui);
  }

  /**
   * スコアメニューGUIを2秒後に再び開く。
   *
   * @param player スコアメニューGUIを開いていたプレイヤー
   */
  public void reopenScoreMenuGui(Player player) {
    Bukkit.getScheduler().runTaskLater(main, () -> openScoreMenuGui(player),
        TickTime.DELAY_2_SECONDS);
  }
}
