package plugin.breakspell.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.manager.effect.SendTextManager;
import plugin.breakspell.game.manager.execution.GameExecutor;
import plugin.breakspell.game.manager.execution.GameStatusChecker;
import plugin.breakspell.menu.MenuGuiManager;

/**
 * ゲームの実行に関するコマンド。<br>
 * 引数なしの場合はゲームメニューGUIを開き、引数ありの場合は引数に応じて難易度別にゲームを実行、または中止する。<br>
 * 難易度を指定して実行する際、ひとつ前の難易度を未プレイの場合は無効とする。<br>
 * ゲームを実行中にコマンドを実行した場合は中止のみ受け付け、それ以外は無効とする。
 */
public class ChantingCommand extends BaseCommand {

  private final GameStatusChecker gameStatusChecker;
  private final SendTextManager sendTextManager;
  private final GameExecutor gameExecutor;
  private final MenuGuiManager menuGuiManager;

  public ChantingCommand(
      GameStatusChecker gameStatusChecker, SendTextManager sendTextManager,
      GameExecutor gameExecutor, MenuGuiManager menuGuiManager) {

    this.gameStatusChecker = gameStatusChecker;
    this.sendTextManager = sendTextManager;
    this.gameExecutor = gameExecutor;
    this.menuGuiManager = menuGuiManager;
  }

  @Override
  public boolean onExecutePlayerCommand(
      Player player, Command command, String label, String[] args) {

    if (gameStatusChecker.isExecutingGame(player)) {
      if (args.length == 1 && args[0].equals("stop")) {
        gameExecutor.stopGame(player);
        sendTextManager.sendStopGameMessage(player);
      } else {
        sendTextManager.sendDuplicationAlert(player);
      }
      return false;
    }

    if (args.length == 0) {
      menuGuiManager.openGameMenuGui(player);
      return false;
    }

    GameDifficulty gameDifficulty = GameDifficulty.getGameDifficulty(args[0]);
    if (gameDifficulty == null) {
      sendTextManager.sendInputErrorAlertForChanting(player);
      return false;
    }

    if (gameStatusChecker.hasNotPlayedPreDifficulty(player, gameDifficulty)) {
      sendTextManager.sendHasNotPlayedPreDifficultyAlert(player, gameDifficulty);
      return false;
    }

    gameExecutor.awaitExecuteGame(player, gameDifficulty);
    return true;
  }

  @Override
  public boolean onExecuteNpcCommand(CommandSender sender) {
    return false;
  }
}
