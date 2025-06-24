package plugin.breakspell.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import plugin.breakspell.Main;
import plugin.breakspell.game.constant.TickTime;
import plugin.breakspell.game.manager.execution.GameStatusChecker;

/**
 * ゲーム実行中の操作を制限するクラス。<br>
 * ゲーム実行中のプレイヤーによるインベントリやアイテムの操作、ブロックの破壊を禁止し、負傷しないようにする。<br>
 * ゲーム実行中に出現させたエンティティも負傷しないようにする。
 */
public class OperationRestriction implements Listener {

  private final Main main;
  private final GameStatusChecker gameStatusChecker;

  public OperationRestriction(Main main, GameStatusChecker gameStatusChecker) {
    this.main = main;
    this.gameStatusChecker = gameStatusChecker;
  }

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent e) {
    if (e.getPlayer() instanceof Player player && gameStatusChecker.isExecutingGame(player)) {
      e.setCancelled(true);

      Bukkit.getScheduler().runTaskLater(main, player :: closeInventory, TickTime.DELAY_BIT);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (e.getWhoClicked() instanceof Player player && gameStatusChecker.isExecutingGame(player)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onDropItem(PlayerDropItemEvent e) {
    if (gameStatusChecker.isExecutingGame(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
    if (gameStatusChecker.isExecutingGame(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onItemHeld(PlayerItemHeldEvent e) {
    if (gameStatusChecker.isExecutingGame(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if (gameStatusChecker.isExecutingGame(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    // ゲームを実行中のプレイヤーが攻撃された場合
    if (e.getEntity() instanceof Player player
        && gameStatusChecker.isExecutingGame(player)) {

      e.setCancelled(true);
      return;
    }

    // ゲームで出現させたエンティティが攻撃された場合
    if (e.getEntity() instanceof LivingEntity livingEntity
        && gameStatusChecker.isGameEntity(livingEntity)) {

      e.setCancelled(true);
    }
  }
}
