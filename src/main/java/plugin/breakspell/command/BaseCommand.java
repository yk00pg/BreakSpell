package plugin.breakspell.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * コマンドの実行処理の基底クラス。
 */
public abstract class BaseCommand implements CommandExecutor {

  public boolean onCommand(
      @NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {

    if (sender instanceof Player player) {
      return onExecutePlayerCommand(player, command, label, args);
    } else {
      return onExecuteNpcCommand(sender);
    }
  }

  /**
   * コマンドの実行者がプレイヤーの場合に実行する。
   *
   * @param player  コマンドを実行したプレイヤー
   * @param command コマンド
   * @param label   ラベル
   * @param args    コマンド引数
   * @return 処理の実行有無
   */
  public abstract boolean onExecutePlayerCommand(
      Player player, Command command, String label, String[] args);

  /**
   * コマンドの実行者がプレイヤー以外の場合に実行する。
   *
   * @param sender コマンドの実行者
   * @return 処理の実行有無
   */
  public abstract boolean onExecuteNpcCommand(CommandSender sender);
}
