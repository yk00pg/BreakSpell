package plugin.breakspell.game.manager.execution;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;
import plugin.breakspell.game.constant.EntityPair;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.Offsets;
import plugin.breakspell.game.data.EntityData;
import plugin.breakspell.game.data.PlayerGameData;
import plugin.breakspell.game.manager.effect.VisualEffectManager;

/**
 * エンティティを出現させるクラス。
 */
public class SpawnEntityManager {

  private final VisualEffectManager visualEffectManager;

  private static final String CURSED_ENTITY_TEAM = "cursed_entity";
  private static final String TRUE_NATURE_TEAM = "true_nature";

  public SpawnEntityManager(VisualEffectManager visualEffectManager) {
    this.visualEffectManager = visualEffectManager;
  }

  /**
   * エンティティを出現させる。<br>
   * 難易度に応じた呪われたエンティティリスト、真の姿のエンティティリスト、x軸とz軸のリストを取得する。<br>
   * 真の姿のエンティティのリストはシャッフルしてランダムに振り分けられるようにする。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param gameDifficulty ゲームの難易度
   * @param playerGameData プレイヤーのゲームデータ
   * @param entityDataMap  エンティティデータマップ
   */
  public void spawnInitialCursedEntity(
      Player player, GameDifficulty gameDifficulty,
      PlayerGameData playerGameData, Map<LivingEntity, EntityData> entityDataMap) {

    List<EntityType> cursedEntityList = gameDifficulty.getCursedEntityList();
    List<EntityType> trueNatureList = gameDifficulty.getTrueNatureList();
    Collections.shuffle(trueNatureList);
    List<List<Integer>> xZList = gameDifficulty.getXZList();

    for (int en = 0; en < trueNatureList.size(); en++) {
      spawnCursedEntity(player, entityDataMap, cursedEntityList, trueNatureList, xZList, en);
    }

    playerGameData.setSpawnedEntity(true);
  }

  /**
   * 呪いをかけられた姿のエンティティを出現させる。<br>
   * 呪いをかけられた姿のエンティティリストからランダムでエンティティの種類を取得する。<br>
   * プレイヤーの位置とx軸とz軸のリストの値から換算した位置にエンティティを出現させて、視覚効果をつけ、状態を設定する。<br>
   * エンティティデータマップに呪いをかけられたエンティティとエンティティデータを紐付けてマップ形式で保持する。
   *
   * @param player           プレイヤー
   * @param entityDataMap    エンティティデータマップ
   * @param cursedEntityList 呪いをかけられた姿のエンティティリスト
   * @param trueNatureList   真の姿のエンティティリスト
   * @param xZList           x軸とz軸のリスト
   * @param en               エンティティのインデックス
   */
  private void spawnCursedEntity(
      Player player, Map<LivingEntity, EntityData> entityDataMap,
      List<EntityType> cursedEntityList, List<EntityType> trueNatureList,
      List<List<Integer>> xZList, int en) {

    EntityType cursedEntityType =
        cursedEntityList.get(new SplittableRandom().nextInt(cursedEntityList.size()));

    LivingEntity cursedEntity =
        (LivingEntity) player.getWorld().spawnEntity(
            getSpawnLocation(player, xZList, en), cursedEntityType, false);
    visualEffectManager.spawnCursedParticle(player, cursedEntity.getLocation());
    setCursedEntityStatus(player, cursedEntity);

    EntityType trueNatureType = trueNatureList.get(en);
    String trueNatureName = EntityPair.getEntityName(trueNatureType, entityDataMap);
    entityDataMap.put(cursedEntity, new EntityData(trueNatureType, trueNatureName));
  }

  /**
   * エンティティを出現させる位置を取得する。
   *
   * @param player プレイヤー
   * @param xZList x軸とz軸のリスト
   * @param en     エンティティのインデックス
   * @return エンティティを出現させる位置
   */
  private Location getSpawnLocation(
      Player player, List<List<Integer>> xZList, int en) {

    Location playerLocation = player.getLocation();
    double x = playerLocation.getX() + xZList.get(en).getFirst();
    double y = playerLocation.getY() + Offsets.BIT_FLOAT;
    double z = playerLocation.getZ() + xZList.get(en).getLast();

    return new Location(player.getWorld(), x, y, z);
  }

  /**
   * 視覚効果をつけながら呪いをかけられた姿のエンティティを再出現させる。
   *
   * @param player        プレイヤー
   * @param entityDataMap エンティティデータマップ
   * @param trueNature    真の姿のエンティティ
   * @param cursedEntity  呪いをかけられた姿のエンティティ
   * @param entityData    エンティティデータ
   */
  public void respawnCursedEntity(
      Player player, Map<LivingEntity, EntityData> entityDataMap,
      LivingEntity trueNature, LivingEntity cursedEntity, EntityData entityData) {

    Location entityLocation = trueNature.getLocation();
    visualEffectManager.spawnRespawnCursedParticle(player, entityLocation);
    LivingEntity spawnEntity =
        (LivingEntity) player.getWorld().spawnEntity(entityLocation, cursedEntity.getType(), false);
    setCursedEntityStatus(player, spawnEntity);
    entityDataMap.put(spawnEntity, new EntityData(trueNature.getType(), entityData.getTrueNatureName()));
  }

  /**
   * 呪いをかけられた姿のエンティティの状態を設定する。
   *
   * @param cursedEntity 呪いをかけられた姿のエンティティ
   */
  private void setCursedEntityStatus(Player player, LivingEntity cursedEntity) {
    Team cursedTeam = setCursedTeam(player);
    cursedTeam.addEntry(cursedEntity.getUniqueId().toString());

    cursedEntity.setVelocity(new Vector(0, 0, 0));
    cursedEntity.setGravity(false);
    cursedEntity.setInvulnerable(true);
    cursedEntity.setGlowing(true);
    cursedEntity.setSilent(true);

    if (cursedEntity.getType().equals(EntityType.ZOMBIE)) {
      Objects.requireNonNull(cursedEntity.getEquipment())
          .setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    }
  }

  /**
   * 呪いをかけられた姿のエンティティのチームを設定する。
   *
   * @return 呪いをかけられた姿のエンティティのチーム
   */
  private Team setCursedTeam(Player player) {
    Scoreboard entityScoreboard = player.getScoreboard();

    Team cursedTeam = entityScoreboard.getTeam(CURSED_ENTITY_TEAM);
    if (cursedTeam == null) {
      cursedTeam = entityScoreboard.registerNewTeam(CURSED_ENTITY_TEAM);
      cursedTeam.setColor(ChatColor.DARK_PURPLE);
      cursedTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
    }
    return cursedTeam;
  }

  /**
   * エンティティデータから真の姿のエンティティの種類を取得し、クリックされたエンティティの位置に出現させる。<br>
   * エンティティデータから真の姿のエンティティの名前を取得し、出現させたエンティティに設定する。<br>
   * エンティティのデータを生成し、真の姿のエンティティと紐付けてマップ形式で保持する。
   *
   * @param player        ゲームを実行中のプレイヤー
   * @param entityDataMap エンティティデータマップ
   * @param entityData    エンティティデータ
   * @param clickedEntity クリックされたエンティティ
   * @return 真の姿のエンティティ
   */
  public LivingEntity spawnTrueNature(
      Player player, Map<LivingEntity, EntityData> entityDataMap,
      EntityData entityData, LivingEntity clickedEntity) {

    EntityType trueNatureType = entityData.getTransformedEntityType();
    LivingEntity trueNature =
        (LivingEntity) player.getWorld().spawnEntity(
            clickedEntity.getLocation(), trueNatureType, false);
    String trueNatureName = entityData.getTrueNatureName();
    setTrueNatureStatus(player, trueNature, trueNatureName);
    entityDataMap.put(trueNature, new EntityData(clickedEntity.getType(), trueNatureName));
    return trueNature;
  }

  /**
   * 真の姿のエンティティの状態と名前を設定する。
   *
   * @param trueNature     真の姿のエンティティ
   * @param trueNatureName 真の姿のエンティティの名前
   */
  private void setTrueNatureStatus(
      Player player, LivingEntity trueNature, String trueNatureName) {

    Team trueNatureTeam = setTrueNatureTeam(player);
    trueNatureTeam.addEntry(trueNature.getUniqueId().toString());

    trueNature.setVelocity(new Vector(0, 0, 0));
    trueNature.setGravity(false);
    trueNature.setInvulnerable(true);
    trueNature.setGlowing(true);
    if (trueNatureName != null) {
      trueNature.setCustomName(trueNatureName);
      trueNature.setCustomNameVisible(true);
    }
  }

  /**
   * 真の姿のエンティティのチームを設定する。
   *
   * @return 真の姿のエンティティのチーム
   */
  private Team setTrueNatureTeam(Player player) {
    Scoreboard entityScoreboard = player.getScoreboard();

    Team trueNatureTeam = entityScoreboard.getTeam(TRUE_NATURE_TEAM);
    if (trueNatureTeam == null) {
      trueNatureTeam = entityScoreboard.registerNewTeam(TRUE_NATURE_TEAM);
      trueNatureTeam.setColor(ChatColor.GOLD);
      trueNatureTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
    }
    return trueNatureTeam;
  }
}