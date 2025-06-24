package plugin.breakspell.listener;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import plugin.breakspell.Main;
import plugin.breakspell.database.PlayerScoreConnector;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.TickTime;
import plugin.breakspell.game.manager.effect.SendTextManager;
import plugin.breakspell.game.manager.effect.WrittenBookManager;
import plugin.breakspell.game.manager.execution.GameExecutor;
import plugin.breakspell.game.manager.execution.GameStatusChecker;
import plugin.breakspell.menu.GameMenu;
import plugin.breakspell.menu.MenuGui;
import plugin.breakspell.menu.MenuGuiManager;
import plugin.breakspell.menu.ScoreMenu;

/**
 * インベントリをクリックした際に発火するイベントのリスナークラス。
 */
public class InventoryClickListener implements Listener {

  private final Main main;
  private final WrittenBookManager writtenBookManager;
  private final GameStatusChecker gameStatusChecker;
  private final GameExecutor gameExecutor;
  private final MenuGuiManager menuGuiManager;
  private final SendTextManager sendTextManager;
  private final PlayerScoreConnector playerScoreConnector;

  public InventoryClickListener(
      Main main, WrittenBookManager writtenBookManager, GameStatusChecker gameStatusChecker,
      GameExecutor gameExecutor, MenuGuiManager menuGuiManager,
      SendTextManager sendTextManager, PlayerScoreConnector playerScoreConnector) {

    this.main = main;
    this.writtenBookManager = writtenBookManager;
    this.gameStatusChecker = gameStatusChecker;
    this.gameExecutor = gameExecutor;
    this.menuGuiManager = menuGuiManager;
    this.sendTextManager = sendTextManager;
    this.playerScoreConnector = playerScoreConnector;
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {

    String guiTitle = e.getView().getTitle();
    boolean isMenuGui = guiTitle.equals(MenuGui.GAME_MENU) || guiTitle.equals(MenuGui.SCORE_MENU);
    boolean hasItem = e.getCurrentItem() != null;
    if (!(e.getWhoClicked() instanceof Player player) || !isMenuGui || !hasItem) {
      return;
    }

    e.setCancelled(true);

    int clickedSlot = e.getRawSlot();
    if (guiTitle.equals(MenuGui.GAME_MENU)) {
      handleGameMenuClick(player, clickedSlot);
    } else {
      handleScoreMenuClick(player, clickedSlot);
    }
  }

  /**
   * ゲームメニューGUIをクリックした場合に、メニューに応じて分岐して処理を実行する。<br>
   * クリックされたスロットの番号からメニューを判別してゲームを開始、またはプロローグ/エピローグの書かれた本を開く。
   *
   * @param player      ゲームメニューGUIをクリックしたプレイヤー
   * @param clickedSlot クリックされたスロットの番号
   */
  private void handleGameMenuClick(Player player, int clickedSlot) {
    GameMenu.getFilteredGameMenu(clickedSlot).ifPresent(
        gameMenu -> {
          switch (gameMenu) {
            case PROLOGUE -> handleClickPrologueMenu(player);
            case EASY, NORMAL, HARD -> handleClickGameDifficultyMenu(player, gameMenu);
            case EPILOGUE -> handleClickEpilogueMenu(player);
            case CLOSE -> player.closeInventory();
          }
        });
  }

  /**
   * プロローグメニューをクリックした場合の処理を実行する。<br>
   * プレイしたことがなければメッセージを表示し、プレイしたことがあればプロローグが書かれた本を開く。
   *
   * @param player プロローグメニューをクリックしたプレイヤー
   */
  private void handleClickPrologueMenu(Player player) {
    if (gameStatusChecker.isFirstPlay(player)) {
      player.closeInventory();
      sendTextManager.sendCannotOpenPrologueAlert(player);
      menuGuiManager.reopenGameMenuGui(player);
    } else {
      writtenBookManager.openPrologueBook(player);

    }
  }

  /**
   * イージー、ノーマル、ハードメニューをクリックした場合の処理を実行する。<br>
   * ひとつ前のレベルをプレイしたことがあるかを確認し、ない場合はメッセージを表示し、ある場合はゲームを実行する。
   *
   * @param player   難易度メニューをクリックしたプレイヤー
   * @param gameMenu ゲームメニュー
   */
  private void handleClickGameDifficultyMenu(Player player, GameMenu gameMenu) {
    GameDifficulty gameDifficulty = GameMenu.getDifficulty(gameMenu);
    if (gameStatusChecker.hasNotPlayedPreDifficulty(player, gameDifficulty)) {
      player.closeInventory();
      sendTextManager.sendHasNotPlayedPreDifficultyAlert(player, gameDifficulty);
      menuGuiManager.reopenGameMenuGui(player);
      return;
    }
    player.closeInventory();
    Bukkit.getScheduler().runTaskLater(main, () ->
        gameExecutor.awaitExecuteGame(player, gameDifficulty), TickTime.DELAY_SHORT);
  }

  /**
   * エピローグメニューをクリックした場合の処理を実行する。<br>
   * ゲームをクリアしたことがない場合はメッセージを表示し、ある場合はエピローグが書かれた本を開く。
   *
   * @param player 　エピローグメニューをクリックしたプレイヤー
   */
  private void handleClickEpilogueMenu(Player player) {
    if (gameStatusChecker.isFirstClear(player)) {
      player.closeInventory();
      sendTextManager.sendCannotOpenEpilogueAlert(player);
      menuGuiManager.reopenGameMenuGui(player);
      return;
    }
    writtenBookManager.openEpilogueBook(player);
  }

  /**
   * スコアメニューGUIをクリックした場合に、メニューに応じて分岐して処理を実行する。<br>
   * 該当スコアがある場合はクリックされたスロットの番号からメニューを判別してスコアリストを表示し、GUIを閉じ、
   * ない場合はメッセージを表示してGUIを再表示する。
   *
   * @param player      スコアメニューGUIをクリックしたプレイヤー
   * @param clickedSlot クリックされたスロットの番号
   */
  private void handleScoreMenuClick(Player player, int clickedSlot) {
    ScoreMenu.getFilteredScoreMenu(clickedSlot).ifPresent(
        scoreMenu -> {
          switch (scoreMenu) {
            case NEW -> {
              if (!playerScoreConnector.showNewlyScoreList(player)) {
                menuGuiManager.reopenScoreMenuGui(player);
              }
            }
            case RANK -> {
              if (!playerScoreConnector.showRankedScoreList(player)) {
                menuGuiManager.reopenScoreMenuGui(player);
              }
            }
            case EASY, NORMAL, HARD -> {
              GameDifficulty gameDifficulty = ScoreMenu.getDifficulty(scoreMenu);
              if (!playerScoreConnector.showRankedByDifficultyScoreList(
                  player, Objects.requireNonNull(gameDifficulty))) {
                menuGuiManager.reopenScoreMenuGui(player);
              }
            }
            case CLOSE -> player.closeInventory();
          }
          player.closeInventory();
        });
  }
}
