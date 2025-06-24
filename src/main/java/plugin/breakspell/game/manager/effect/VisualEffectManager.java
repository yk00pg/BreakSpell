package plugin.breakspell.game.manager.effect;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import plugin.breakspell.Main;
import plugin.breakspell.game.constant.Offsets;
import plugin.breakspell.game.constant.TickTime;

/**
 * 演出用の視覚効果を出現させるクラス。
 */
public class VisualEffectManager {

  private final Main main;

  // パーティクルの量（count）を定数化
  private static final int TINY = 3;
  private static final int LITTLE_BIT = 5;
  private static final int FEW = 15;
  private static final int MODERATE = 30;
  private static final int LOT = 50;

  // パーティクルの広がり（extra）を定数化
  private static final double ALMOST_STOP = 0.005;
  private static final double SLIGHTLY_MOVE = 0.05;

  public VisualEffectManager(Main main) {
    this.main = main;
  }

  /**
   * 魔法の本を書見台に置いた際のパーティクルを出現させる。
   *
   * @param player   ゲームを実行中のプレイヤー
   * @param location 書見台があるブロック
   */
  public void spawnMagicBookOnLecternParticle(Player player, Location location) {
    final int MAGIC_FLASH_COUNT = 3;
    for (int p = 0; p < MAGIC_FLASH_COUNT; p++) {
      Bukkit.getScheduler().runTaskLater(main, () ->
              player.spawnParticle(Particle.FLASH, location, LITTLE_BIT,
                  0, Offsets.LITTLE_ABOVE, Offsets.LITTLE_SOUTH)
          , TickTime.DELAY_LONG * p);
    }
  }

  /**
   * 魔法が発動して書見台が消滅する際のパーティクルを出現させる。
   *
   * @param player   ゲームを実行中のプレイヤー
   * @param location 書見台があるブロック
   */
  public void spawnMagicBeginParticle(Player player, Location location) {

    double[] offsetXs = {Offsets.LITTLE_EAST, Offsets.LITTLE_WEST};
    Location lecternLoc = location.clone();

    for (double OffsetX : offsetXs) {
      Location particleLoc = lecternLoc.clone().add(OffsetX, Offsets.TINY_BIT_ABOVE, 0);
      player.spawnParticle(
          Particle.GLOW, particleLoc, MODERATE, 0, 0, 0, ALMOST_STOP);
      player.spawnParticle(
          Particle.ENCHANT, particleLoc, MODERATE, 0, 0, 0, ALMOST_STOP);
      player.spawnParticle(
          Particle.WITCH, particleLoc, MODERATE, 0, 0, 0, ALMOST_STOP);
    }
    player.spawnParticle(
        Particle.CLOUD, lecternLoc, LOT, 0, 0, Offsets.LITTLE_SOUTH, SLIGHTLY_MOVE);
    player.spawnParticle(
        Particle.WHITE_ASH, lecternLoc, FEW,
        0, Offsets.TINY_BIT_ABOVE, Offsets.LITTLE_SOUTH, ALMOST_STOP);
  }

  /**
   * ゲームオーバーになった際にパーティクルを出現させる。
   *
   * @param player 　ゲームを実行中のプレイヤー
   */
  public void spawnGameOverParticle(Player player) {
    player.spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), TINY);
  }

  /**
   * ハイスコアを更新した際に花火を出現させる。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void spawnFireworksForHighScore(Player player) {
    List<Type> typeList = List.of(Type.BALL_LARGE, Type.BALL, Type.STAR, Type.BALL_LARGE, Type.STAR, Type.BALL);

    int f = 1;
    for (Type type : typeList) {
      Bukkit.getScheduler().runTaskLater(main, () -> {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(
            FireworkEffect.builder()
                .withColor(Color.FUCHSIA)
                .withColor(Color.AQUA)
                .withColor(Color.LIME)
                .withFade(Color.YELLOW)
                .withFade(Color.WHITE)
                .with(type)
                .withFlicker()
                .withTrail()
                .build());
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
      }, TickTime.DELAY_SHORT * f);
      f++;
    }
  }

  /**
   * 呪いをかけられた姿のエンティティが出現する際のパーティクルを出現させる。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param entityLocation エンティティの位置
   */
  public void spawnCursedParticle(Player player, Location entityLocation) {
    player.spawnParticle(Particle.DRAGON_BREATH, entityLocation, LOT);
  }

  /**
   * クリックしたエンティティの位置に、魔法をかけるパーティクルを出現させる。
   *
   * @param player                ゲームを実行中のプレイヤー
   * @param clickedEntityLocation 　クリックしたエンティティの位置
   */
  public void spawnEnchantParticle(Player player, Location clickedEntityLocation) {
    player.getWorld().spawnParticle(
        Particle.GLOW, clickedEntityLocation, MODERATE, 0, Offsets.LITTLE_ABOVE, 0);
  }

  /**
   * エンティティをシャッフルする際のパーティクルを出現させる。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param entityLocation エンティティの位置
   */
  public void spawnShuffleParticle(Player player, Location entityLocation) {
    player.getWorld().spawnParticle(
        Particle.GUST_EMITTER_SMALL, entityLocation, MODERATE);
  }

  /**
   * ペアが揃った際のコンボメッセージをアーマースタンドで表示し、0.5秒後に消滅させる。
   *
   * @param player       ゲームを実行中のプレイヤー
   * @param trueNature   真の姿のエンティティ
   * @param comboMessage コンボメッセージ
   */
  public void spawnComboMessageArmorStand(
      Player player, LivingEntity trueNature, String comboMessage) {

    ArmorStand armorStand
        = player.getWorld().spawn(
        trueNature.getLocation().clone().add(0, Offsets.ALMOST_EYE_LEVEL, 0), ArmorStand.class);
    armorStand.setCustomName(
        ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + ChatColor.ITALIC + ChatColor.UNDERLINE
            + comboMessage);
    armorStand.setCustomNameVisible(true);
    armorStand.setInvisible(true);
    armorStand.setMarker(true);
    armorStand.setGravity(false);

    Bukkit.getScheduler().runTaskLater(main, armorStand :: remove, TickTime.DELAY_LONG);
  }

  /**
   * 真の姿のエンティティを解放する（消滅させる）際のパーティクルを出現させる。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param entityLocation エンティティの位置
   */
  public void spawnFreeTrueNatureParticle(Player player, Location entityLocation) {
    player.getWorld().spawnParticle(
        Particle.CHERRY_LEAVES, entityLocation, LOT, 0, Offsets.ALMOST_EYE_LEVEL, 0, SLIGHTLY_MOVE);
  }

  /**
   * 呪いをかけられた姿のエンティティを再出現させる際のパーティクルを出現させる。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param entityLocation エンティティの位置
   */
  public void spawnRespawnCursedParticle(Player player, Location entityLocation) {
    player.getWorld().spawnParticle(
        Particle.WITCH, entityLocation, LOT, 0, Offsets.ABOVE, 0);
  }
}
