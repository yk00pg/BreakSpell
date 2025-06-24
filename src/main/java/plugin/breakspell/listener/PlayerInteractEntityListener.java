package plugin.breakspell.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import plugin.breakspell.Main;
import plugin.breakspell.game.constant.EntityPair;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.SpecialEntity;
import plugin.breakspell.game.constant.TickTime;
import plugin.breakspell.game.data.EntityData;
import plugin.breakspell.game.data.PendingEntityData;
import plugin.breakspell.game.data.PlayerGameData;
import plugin.breakspell.game.manager.effect.ScoreboardManager;
import plugin.breakspell.game.manager.effect.SendTextManager;
import plugin.breakspell.game.manager.effect.SoundEffectManager;
import plugin.breakspell.game.manager.effect.VisualEffectManager;
import plugin.breakspell.game.manager.execution.ComboBonusManager;
import plugin.breakspell.game.manager.execution.SpawnEntityManager;

/**
 * プレイヤーがエンティティをクリックした際に発火するイベントのリスナークラス。
 */
public class PlayerInteractEntityListener implements Listener {

  private final Main main;
  private final SendTextManager sendTextManager;
  private final SoundEffectManager soundEffectManager;
  private final VisualEffectManager visualEffectManager;
  private final SpawnEntityManager spawnEntityManager;
  private final ComboBonusManager comboBonusManager;
  private final ScoreboardManager scoreboardManager;

  private final Map<UUID, PlayerGameData> playerDataMap;

  private static final long VALID_CLICK_TIME = 500;

  public PlayerInteractEntityListener(
      Main main, SendTextManager sendTextManager, SoundEffectManager soundEffectManager,
      VisualEffectManager visualEffectManager, SpawnEntityManager spawnEntityManager,
      ComboBonusManager comboBonusManager, ScoreboardManager scoreboardManager,
      Map<UUID, PlayerGameData> playerDataMap) {

    this.main = main;
    this.sendTextManager = sendTextManager;
    this.soundEffectManager = soundEffectManager;
    this.visualEffectManager = visualEffectManager;
    this.spawnEntityManager = spawnEntityManager;
    this.comboBonusManager = comboBonusManager;
    this.scoreboardManager = scoreboardManager;
    this.playerDataMap = playerDataMap;
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
    Player player = e.getPlayer();
    UUID uuid = player.getUniqueId();

    PlayerGameData playerGameData = playerDataMap.get(uuid);
    if (playerGameData == null) {
      return;
    }

    if (e.getHand() != EquipmentSlot.HAND) {
      return;
    }

    if (isDisableClick(playerGameData)) {
      return;
    }

    Map<LivingEntity, EntityData> entityDataMap = playerGameData.getEntityDataMap();
    Map<LivingEntity, PendingEntityData> awaitingPairMap = playerGameData.getAwaitingPairMap();

    LivingEntity clickedEntity = (LivingEntity) e.getRightClicked();
    if (!entityDataMap.containsKey(clickedEntity)
        || awaitingPairMap.containsKey(clickedEntity)) {
      return;
    }

    EntityData entityData = entityDataMap.remove(clickedEntity);
    if (entityData == null) {
      return;
    }

    Location clickedEntityLocation = clickedEntity.getLocation();
    addMagicalEffect(player, clickedEntityLocation);

    PendingEntityData pendingEntityData
        = new PendingEntityData(clickedEntity, entityData);
    clickedEntity.remove();

    LivingEntity trueNature =
        spawnEntityManager.spawnTrueNature(player, entityDataMap, entityData, clickedEntity);

    if (checkAndHandleSpecialEntity(player, playerGameData, entityDataMap, trueNature)) {
      return;
    }

    sendTextManager.sendRevealTrueNatureMessage(player, trueNature.getCustomName());
    awaitingPairMap.put(trueNature, pendingEntityData);

    if (awaitingPairMap.size() == 1) {
      sendTextManager.sendFirstClickMessage(player);
    } else {
      checkAndHandlePair(player, entityDataMap, awaitingPairMap, trueNature);
    }
  }

  /**
   * 前のクリックから0.5秒未満にクリックされた場合は無効なクリックと判断して処理を抜け、
   * そうでない場合はクリックした時間を更新して処理を続ける。
   *
   * @param playerGameData プレイヤーのゲームデータ
   * @return クリックが無効かどうか
   */
  private boolean isDisableClick(PlayerGameData playerGameData) {
    long now = System.currentTimeMillis();
    if ((now - playerGameData.getClickedTime()) < VALID_CLICK_TIME) {
      return true;
    }
    playerGameData.updateClickedTime(now);
    return false;
  }

  /**
   * 魔法のような視覚効果をつける。<br>
   * プレイヤーにメインハンドを振らせて魔法の言葉を表示し、
   * サウンドを流してクリックしたエンティティの位置に魔法をかけるパーティクルを出現させる。
   *
   * @param player                ゲームを実行中のプレイヤー
   * @param clickedEntityLocation クリックしたエンティティの位置
   */
  private void addMagicalEffect(Player player, Location clickedEntityLocation) {
    player.swingMainHand();
    sendTextManager.sendMagicWordMassage(player);
    soundEffectManager.playWaveMagicWandSound(player);
    visualEffectManager.spawnEnchantParticle(player, clickedEntityLocation);
  }

  /**
   * 出現させた真の姿のエンティティがスペシャルエンティティかどうか判定し、分岐して処理を実行する。<br>
   * アレイの場合は5秒プラス、ガストの場合は5秒マイナス、エンダーマンの場合は位置をエンティティの位置をシャッフルする。<br>
   * スペシャルエンティティの名前と特殊効果をメッセージで表示し、スペシャルエンティティを消滅させる。
   *
   * @param player        ゲームを実行中のプレイヤー
   * @param entityDataMap エンティティデータマップ
   * @param trueNature    真の姿のエンティティ
   */
  private boolean checkAndHandleSpecialEntity(
      Player player, PlayerGameData playerGameData,
      Map<LivingEntity, EntityData> entityDataMap, LivingEntity trueNature) {

    SpecialEntity specialEntity = SpecialEntity.getFilteredSpecialEntity(trueNature.getType());
    if (specialEntity == null) {
      return false;
    }

    switch (specialEntity) {
      case ALLAY -> playerGameData.addTime(5);
      case SHULKER -> playerGameData.reduceTime(5);
      case ENDERMAN -> shuffleEntityLocation(player, entityDataMap);
    }

    sendTextManager.sendRevealSpecialEntityMessage(player, specialEntity);
    soundEffectManager.playRevealSpecialEntitySound(player, specialEntity);
    entityDataMap.remove(trueNature);
    Bukkit.getScheduler().runTaskLater(main, trueNature :: remove, TickTime.DELAY_LONG);
    return true;
  }

  /**
   * エンティティの位置をシャッフルする。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  private void shuffleEntityLocation(
      Player player, Map<LivingEntity, EntityData> entityDataMap) {

    List<LivingEntity> entityList = new ArrayList<>(entityDataMap.keySet());
    List<Location> locationList = new ArrayList<>();
    for (LivingEntity livingEntity : entityList) {
      locationList.add(livingEntity.getLocation().clone());
      visualEffectManager.spawnShuffleParticle(player, livingEntity.getLocation());
    }

    Collections.shuffle(locationList);

    for (int i = 0; i < entityList.size(); i++) {
      entityList.get(i).teleport(locationList.get(i));
    }
  }

  /**
   * 1体目のエンティティと2体目のエンティティがペアかどうかを確認し、分岐して処理を実行する。<br>
   * ペア待ちマップをクリアする。
   *
   * @param player           プレイヤー
   * @param entityDataMap    エンティティデータマップ
   * @param awaitingPairMap  ペア待ちマップ
   * @param secondTrueNature 2体目の真の姿のエンティティ
   */
  private void checkAndHandlePair(
      Player player, Map<LivingEntity, EntityData> entityDataMap,
      Map<LivingEntity, PendingEntityData> awaitingPairMap, LivingEntity secondTrueNature) {

    LivingEntity firstTrueNature = awaitingPairMap.keySet().iterator().next();
    PendingEntityData firstPendingEntityData = awaitingPairMap.remove(firstTrueNature);

    String firstTrueNatureName = firstPendingEntityData.getEntityData().getTrueNatureName();
    String pairName = EntityPair.getPairEntityName(firstTrueNature.getType(), firstTrueNatureName);

    PlayerGameData playerGameData = playerDataMap.get(player.getUniqueId());
    if (Objects.equals(secondTrueNature.getCustomName(), pairName)) {
      handleMatchPair(player, playerGameData, entityDataMap, firstTrueNature, secondTrueNature);
    } else {
      handleMismatchPair(
          player, playerGameData, entityDataMap, awaitingPairMap,
          firstPendingEntityData, secondTrueNature, firstTrueNature);
    }
    awaitingPairMap.clear();
  }

  /**
   * ペアが揃った場合に、ペア数を加算し連続ペア数とスコアを更新する。<br>
   * スコアを加算してメッセージを表示し、コンボメッセージを浮かばせる。<br>
   * 視覚効果をつけて真の姿のエンティティを解放する（消滅させる）。
   *
   * @param player           プレイヤー
   * @param playerGameData   プレイヤーのゲームデータ
   * @param entityDataMap    エンティティデータマップ
   * @param firstTrueNature  1体目の真の姿のエンティティ
   * @param secondTrueNature 2体目の真の姿のエンティティ
   */
  private void handleMatchPair(
      Player player, PlayerGameData playerGameData, Map<LivingEntity, EntityData> entityDataMap,
      LivingEntity firstTrueNature, LivingEntity secondTrueNature) {

    playerGameData.addMatchedPair();
    playerGameData.updateConsecutivePairs();

    int consecutivePairs = playerGameData.getConsecutivePairs();
    updateScore(player, playerGameData, consecutivePairs);

    sendTextManager.sendMatchPairMessage(player);

    String comboMessage = comboBonusManager.getComboMessage(consecutivePairs);
    visualEffectManager.spawnComboMessageArmorStand(player, firstTrueNature, comboMessage);
    visualEffectManager.spawnComboMessageArmorStand(player, secondTrueNature, comboMessage);
    soundEffectManager.playMatchPairSound(player);

    Bukkit.getScheduler().runTaskLater(main, () -> {
      releaseTrueNature(player, entityDataMap, firstTrueNature);
      releaseTrueNature(player, entityDataMap, secondTrueNature);
    }, TickTime.DELAY_LONG);
  }

  /**
   * ゲームの難易度に応じたポイントと連続ペア数に応じたコンボボーナスを掛け合わせてスコアに加算し、スコアボードを更新する。
   *
   * @param player           ゲームを実行中のプレイヤー
   * @param playerGameData   プレイヤーのゲームデータ
   * @param consecutivePairs 連続ペア数
   */
  public void updateScore(Player player, PlayerGameData playerGameData, int consecutivePairs) {
    GameDifficulty gameDifficulty = playerGameData.getGameDifficulty();
    int addPoint = (int) Math.round(
        gameDifficulty.getPoint()
            * comboBonusManager.getComboBonus(consecutivePairs, gameDifficulty));
    playerGameData.addScore(addPoint);
    scoreboardManager.updateCurrentScore(player, playerGameData);
  }

  /**
   * 真の姿のエンティティを視覚効果をつけて解放する（消滅させる）。
   *
   * @param player        ゲームを実行中のプレイヤー
   * @param entityDataMap エンティティデータマップ
   * @param trueNature    真の姿のエンティティ
   */
  private void releaseTrueNature(
      Player player, Map<LivingEntity, EntityData> entityDataMap, LivingEntity trueNature) {

    visualEffectManager.spawnFreeTrueNatureParticle(player, trueNature.getLocation());
    trueNature.remove();
    entityDataMap.remove(trueNature);
  }

  /**
   * ペアが揃わなかった場合に、メッセージを表示してサウンドを流し、連続ペア数をリセットする。<br>
   * 残り時間が0.5秒以上の場合、呪われた姿のエンティティを再出現させて真の姿のエンティティを消滅させる。
   *
   * @param player                 プレイヤー
   * @param playerGameData         プレイヤーデータ
   * @param firstPendingEntityData 1体目の待機中のエンティティデータ
   * @param secondTrueNature       2体目の真の姿のエンティティ
   * @param firstTrueNature        1体目の真の姿のエンティティ
   */
  private void handleMismatchPair(
      Player player, PlayerGameData playerGameData, Map<LivingEntity, EntityData> entityDataMap,
      Map<LivingEntity, PendingEntityData> awaitingPairMap, PendingEntityData firstPendingEntityData,
      LivingEntity secondTrueNature, LivingEntity firstTrueNature) {

    sendTextManager.sendMisMatchMessage(player);
    soundEffectManager.playMisMatchPairSound(player);

    playerGameData.resetConsecutivePairs();
    scoreboardManager.updateCurrentScore(player, playerGameData);

    if (playerGameData.getGameTime() >= 0.5) {
      LivingEntity firstCursedEntity = firstPendingEntityData.getCursedEntity();
      EntityData firstEntityData = firstPendingEntityData.getEntityData();
      PendingEntityData secondPendingEntityData = awaitingPairMap.remove(secondTrueNature);
      LivingEntity secondCursedEntity = secondPendingEntityData.getCursedEntity();
      EntityData secondEntityData = secondPendingEntityData.getEntityData();

      Bukkit.getScheduler().runTaskLater(main, () -> {
        spawnEntityManager.respawnCursedEntity(
            player, entityDataMap, firstTrueNature, firstCursedEntity, firstEntityData);
        spawnEntityManager.respawnCursedEntity(
            player, entityDataMap, secondTrueNature, secondCursedEntity, secondEntityData);
        firstTrueNature.remove();
        secondTrueNature.remove();
        entityDataMap.remove(firstTrueNature);
        entityDataMap.remove(secondTrueNature);
      }, TickTime.DELAY_LONG);
    }
  }
}
