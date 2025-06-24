package plugin.breakspell.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.breakspell.database.PlayerScoreConnector;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.manager.effect.SendTextManager;
import plugin.breakspell.game.manager.execution.GameStatusChecker;
import plugin.breakspell.menu.MenuGuiManager;

/**
 * スコアリストの表示に関するコマンド。<br>
 * 引数なしの場合はスコアメニューGUIを開き、引数ありの場合は引数に応じて
 * 新着・全体ランキング・難易度別ランキングのスコアリストを表示する。<br>
 * ゲームを実行中にコマンドを実行した場合は無効とする。
 */
public class ScoreCommand extends BaseCommand {

  private final GameStatusChecker gameStatusChecker;
  private final SendTextManager sendTextManager;
  private final MenuGuiManager menuGuiManager;
  private final PlayerScoreConnector playerScoreConnector;

  public ScoreCommand(
      GameStatusChecker gameStatusChecker, SendTextManager sendTextManager,
      MenuGuiManager menuGuiManager, PlayerScoreConnector playerScoreConnector) {

    this.gameStatusChecker = gameStatusChecker;
    this.sendTextManager = sendTextManager;
    this.menuGuiManager = menuGuiManager;
    this.playerScoreConnector = playerScoreConnector;
  }

  @Override
  public boolean onExecutePlayerCommand(
      Player player, Command command, String label, String[] args) {

    if (gameStatusChecker.isExecutingGame(player)) {
      sendTextManager.sendDuplicationAlert(player);
      return false;
    }

    if (args.length == 0) {
      menuGuiManager.openScoreMenuGui(player);
      return false;
    }

    if (args.length == 1) {
      switch (args[0]) {
        case "new" -> playerScoreConnector.showNewlyScoreList(player);
        case "rank" -> playerScoreConnector.showRankedScoreList(player);
        case "easy", "normal", "hard" -> {
          GameDifficulty gameDifficulty = GameDifficulty.getGameDifficulty(args[0]);
          playerScoreConnector.showRankedByDifficultyScoreList(player, gameDifficulty);
        }
        default -> {
          sendTextManager.sendInputAlertForScoreList(player);
          return false;
        }
      }
      return true;
    } else {
      sendTextManager.sendInputAlertForScoreList(player);
      return false;
    }
  }

  @Override
  public boolean onExecuteNpcCommand(CommandSender sender) {
    return false;
  }
}
