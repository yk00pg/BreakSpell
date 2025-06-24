package plugin.breakspell.game.manager.execution;

import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lectern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import plugin.breakspell.Main;
import plugin.breakspell.database.PlayerProgressConnector;
import plugin.breakspell.database.PlayerScoreConnector;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.Offsets;
import plugin.breakspell.game.constant.TickTime;
import plugin.breakspell.game.data.EntityData;
import plugin.breakspell.game.data.PlayerGameData;
import plugin.breakspell.game.manager.effect.BossBarManager;
import plugin.breakspell.game.manager.effect.ScoreboardManager;
import plugin.breakspell.game.manager.effect.SendTextManager;
import plugin.breakspell.game.manager.effect.SoundEffectManager;
import plugin.breakspell.game.manager.effect.VisualEffectManager;
import plugin.breakspell.game.manager.effect.WrittenBookManager;

/**
 * ゲームを実行するクラス。
 */
public class GameExecutor {

  private final Main main;
  private final GameStatusChecker gameStatusChecker;
  private final WrittenBookManager writtenBookManager;
  private final VisualEffectManager visualEffectManager;
  private final SoundEffectManager soundEffectManager;
  private final SendTextManager sendTextManager;
  private final SpawnEntityManager spawnEntityManager;
  private final ScoreboardManager scoreboardManager;
  private final PlayerScoreConnector playerScoreConnector;
  private final PlayerProgressConnector playerProgressConnector;
  private final BossBarManager bossBarManager = new BossBarManager();

  private final Map<UUID, PlayerGameData> playerGameDataMap;

  public GameExecutor(
      Main main, GameStatusChecker gameStatusChecker, WrittenBookManager writtenBookManager,
      VisualEffectManager visualEffectManager, SoundEffectManager soundEffectManager,
      SendTextManager sendTextManager, SpawnEntityManager spawnEntityManager,
      ScoreboardManager scoreboardManager, PlayerScoreConnector playerScoreConnector,
      PlayerProgressConnector playerProgressConnector, Map<UUID, PlayerGameData> playerGameDataMap) {

    this.main = main;
    this.gameStatusChecker = gameStatusChecker;
    this.writtenBookManager = writtenBookManager;
    this.visualEffectManager = visualEffectManager;
    this.soundEffectManager = soundEffectManager;
    this.sendTextManager = sendTextManager;
    this.spawnEntityManager = spawnEntityManager;
    this.scoreboardManager = scoreboardManager;
    this.playerScoreConnector = playerScoreConnector;
    this.playerProgressConnector = playerProgressConnector;
    this.playerGameDataMap = playerGameDataMap;
  }

  /**
   * ゲームを中止する。
   *
   * @param player ゲーム実行中に中止コマンドを実行したプレイヤー
   */
  public void stopGame(Player player) {
    PlayerGameData playerGameData = playerGameDataMap.get(player.getUniqueId());
    playerGameData.endTime();
  }

  /**
   * ゲームが実行されるのを待つ。<br>
   * 初回プレイ時のみ、プロローグが書かれた本を開く。<br>
   * プレイヤーのメインハンドに魔法の本を持たせ、書見台を設置する。<br>
   * 書見台に魔法の本が設置されたら、ゲームを実行する。
   *
   * @param player         コマンドを実行またはゲームメニューGUIをクリックしたプレイヤー
   * @param gameDifficulty ゲームの難易度
   */
  public void awaitExecuteGame(Player player, GameDifficulty gameDifficulty) {
    double readingTime = 0;
    if (gameStatusChecker.isFirstPlay(player)) {
      writtenBookManager.openPrologueBook(player);
      readingTime = 20;
    }

    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
    ItemStack magicBook = getMagicBook();
    player.getInventory().setItemInMainHand(magicBook);
    Block lecternBlock = getLecternBlock(player);
    LecternInventory lecternInventory
        = (LecternInventory) ((Lectern) lecternBlock.getState()).getInventory();

    sendTextManager.sendStandByMessage(player);
    checkLecternAndExecuteGame(
        player, gameDifficulty, readingTime, mainHandItem, lecternInventory, magicBook, lecternBlock);
  }

  /**
   * 「魔法の本」という名前をつけた記入済みの本を取得する。
   *
   * @return 魔法の本
   */
  @NotNull
  private ItemStack getMagicBook() {
    ItemStack magicBook = new ItemStack(Material.WRITTEN_BOOK);
    ItemMeta magicBookMeta = magicBook.getItemMeta();
    if (magicBookMeta != null) {
      magicBookMeta.setDisplayName(
          ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.ITALIC + "魔法の本");
      magicBook.setItemMeta(magicBookMeta);
    }
    return magicBook;
  }

  /**
   * プレイヤーの位置から2ブロック北側（Z軸-2）に位置するブロックを取得し、南向きに書見台を設置する。
   *
   * @param player コマンドを実行またはゲームメニューGUIをクリックしたプレイヤー
   * @return 書見台を設置したブロック
   */
  @NotNull
  private Block getLecternBlock(Player player) {
    Block lecternBlock
        = player.getLocation().clone().add(0, 0, Offsets.TWO_BLOCK_NORTH).getBlock();
    lecternBlock.setType(Material.LECTERN);

    BlockData blockData = lecternBlock.getBlockData();
    if (blockData instanceof Directional directional) {
      directional.setFacing(BlockFace.SOUTH);
      lecternBlock.setBlockData(directional);
    }
    return lecternBlock;
  }

  /**
   * 0.5秒ごとに書見台に魔法の本が置かれているかを確認し、置かれている場合は確認タスクを中止する。<br>
   * 視覚効果をつけながら書見台を削除し、ゲームを開始する。<br>
   * 10秒（初回は30秒）以内に魔法の本が置かれなかった場合はゲームをキャンセルする。
   *
   * @param player           コマンドを実行またはゲームメニューGUIをクリックしたプレイヤー
   * @param gameDifficulty   ゲームの難易度
   * @param mainHandItem     プレイヤーがメインハンドに持っていたアイテム
   * @param lecternInventory 書見台のインベントリ
   * @param magicBook        魔法の本
   * @param lecternBlock     書見台を設置したブロック
   */
  private void checkLecternAndExecuteGame(
      Player player, GameDifficulty gameDifficulty, double readingTime, ItemStack mainHandItem,
      LecternInventory lecternInventory, ItemStack magicBook, Block lecternBlock) {

    final double[] pendingTime = {10 + readingTime};
    Bukkit.getScheduler().runTaskTimer(main, lecternCheckTask -> {

      ItemStack bookOnLectern = lecternInventory.getItem(0);

      if (pendingTime[0] < 0) {
        lecternCheckTask.cancel();
        handleGameCancellation(player, mainHandItem, lecternBlock);
      }

      if (bookOnLectern != null && bookOnLectern.equals(magicBook)) {
        lecternCheckTask.cancel();
        soundEffectManager.playMagicBookOnLecternSound(player);
        visualEffectManager.spawnMagicBookOnLecternParticle(player, lecternBlock.getLocation());

        Bukkit.getScheduler().runTaskLater(main, () -> {
          lecternBlock.setType(Material.AIR);
          visualEffectManager.spawnMagicBeginParticle(player, lecternBlock.getLocation());
          soundEffectManager.stopMagicBookOnLecternSound(player);
        }, TickTime.DELAY_2_SECONDS);

        executeGame(player, gameDifficulty, mainHandItem);
      }

      sendTextManager.sendPendingTimeActionBar(player, pendingTime);
      pendingTime[0] -= 0.5;

    }, TickTime.DELAY_SHORT, TickTime.PERIOD_HALF_SECOND);
  }

  /**
   * 書見台を消滅させ、メインハンドに持っていたアイテムをプレイヤーに返し、<br/>
   * メッセージを表示して、ゲームをキャンセルする。
   *
   * @param player       コマンドを実行またはゲームメニューGUIをクリックしたプレイヤー
   * @param mainHandItem プレイヤーがメインハンドに持っていたアイテム
   * @param lecternBlock 書見台を設置したブロック
   */
  private void handleGameCancellation(
      Player player, ItemStack mainHandItem, Block lecternBlock) {

    lecternBlock.setType(Material.AIR);
    player.getInventory().setItemInMainHand(mainHandItem);
    sendTextManager.sendCancelGameMessage(player);
  }

  /**
   * プレイヤーのゲームデータを作成し、UUIDと紐付けてマップ形式で保持する。<br>
   * DBにプレイ状況を登録し、ゲームプレイ用にプレイヤーの状態を設定する。<br>
   * ゲーム開始前のカウントダウンを行い、ゲームを実行する。
   *
   * @param player         ゲームを実行したプレイヤー
   * @param gameDifficulty ゲームの難易度
   * @param mainHandItem   プレイヤーがメインハンドに持っていたアイテム
   */
  private void executeGame(
      Player player, GameDifficulty gameDifficulty, ItemStack mainHandItem) {

    UUID playerUuid = player.getUniqueId();
    PlayerGameData playerGameData =
        new PlayerGameData(playerUuid, player.getName(), gameDifficulty, mainHandItem);
    playerGameDataMap.put(playerUuid, playerGameData);

    playerProgressConnector.insertGameProgress(playerUuid, gameDifficulty);
    initPlayerStatus(player);
    countDownToStartGame(player);
    handleGamePlay(player, playerGameData, gameDifficulty);
  }

  /**
   * ゲームのプレイ体験を損なわないよう、プレイヤーの状態を初期化する。<br>
   * 負傷などによる影響が出ないように無敵状態にし、メインハンドに演出用の「魔法の杖」を持たせる。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  private void initPlayerStatus(Player player) {
    player.setInvulnerable(true);
    player.getInventory().setItemInMainHand(getMagicWand());
  }

  /**
   * 「魔法の杖」と名前をつけたブリーズロッドを取得する。
   */
  private ItemStack getMagicWand() {
    ItemStack magicWand = new ItemStack(Material.BREEZE_ROD);
    ItemMeta itemMeta = magicWand.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.ITALIC + "魔法の杖");
    }
    magicWand.setItemMeta(itemMeta);
    return magicWand;
  }

  /**
   * ゲーム開始までのカウントダウンを行い、待機時間に応じてタイトルを表示する。
   *
   * @param player 　ゲームを実行中のプレイヤー
   */
  private void countDownToStartGame(Player player) {
    final int[] idleTime = {5};
    Bukkit.getScheduler().runTaskTimer(main, countDownTask -> {
      switch (idleTime[0]) {
        case 0 -> {
          countDownTask.cancel();
          sendTextManager.sendCountDownToStartGameTitle(player, idleTime[0]);
        }
        case 1, 2, 3, 5 -> sendTextManager.sendCountDownToStartGameTitle(player, idleTime[0]);
      }
      soundEffectManager.playCountDownToStartGameSound(player, idleTime[0]);
      idleTime[0]--;
    }, 0, TickTime.PERIOD_1_SECOND);
  }

  /**
   * ゲームを開始する。<br>
   * ボスバーを設置し、残り時間に応じて処理を分岐してゲームを管理する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   * @param gameDifficulty ゲームの難易度
   */
  private void handleGamePlay(
      Player player, PlayerGameData playerGameData, GameDifficulty gameDifficulty) {

    int initialGameTime = gameDifficulty.getGameTime();
    playerGameData.setGameTime(initialGameTime);

    bossBarManager.createBossBar();

    manageGamePlayByTimeLeft(player, playerGameData, gameDifficulty, initialGameTime);
  }

  /**
   * 残り時間に応じて処理を分岐してゲームを管理する。<br>
   * すべてのペアが揃った場合は、その時点でゲームタスクを中止して処理を実行する。<br>
   * 残り時間が5秒になったらカウントダウンを開始し、残り時間が0になったらゲームタスクを中止して処理を実行する。
   *
   * @param player          ゲームを実行中のプレイヤー
   * @param playerGameData  プレイヤーのゲームデータ
   * @param gameDifficulty  ゲームの難易度
   * @param initialGameTime ゲームの設定時間
   */
  private void manageGamePlayByTimeLeft(
      Player player, PlayerGameData playerGameData,
      GameDifficulty gameDifficulty, int initialGameTime) {

    Map<LivingEntity, EntityData> entityDataMap = playerGameData.getEntityDataMap();

    Bukkit.getScheduler().runTaskTimer(main, gameTask -> {
      int timeLeft = playerGameData.getGameTime();

      if (playerGameData.getMatchedPair() == gameDifficulty.getPairNum()) {
        gameTask.cancel();
        handleFindAllPairs(player, playerGameData, gameDifficulty, entityDataMap, timeLeft);
        return;
      }

      if (timeLeft == initialGameTime && !playerGameData.isSpawnedEntity()) {
        initGameAtFirstTick(player, playerGameData, gameDifficulty, entityDataMap);
      } else if (timeLeft <= 0) {
        gameTask.cancel();
        handleGameEnd(player, playerGameData, gameDifficulty, entityDataMap);
        return;
      } else if (timeLeft <= 5) {
        sendTextManager.sendCountDownToEndGameTitle(player, timeLeft);
        soundEffectManager.playCountDownToEndGameSound(player, timeLeft);
      }

      soundEffectManager.playCursedEntitySound(player, gameDifficulty);
      bossBarManager.updateBossBar(timeLeft, initialGameTime);
      playerGameData.reduceTime(1);
    }, TickTime.DELAY_5_SECONDS, TickTime.PERIOD_1_SECOND);
  }

  /**
   * 初めのtickでゲームの初期化処理を行う。<br>
   * スコアボードを作成し、呪いをかけられた姿のエンティティを出現させ、ボスバーをプレイヤーに表示する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   * @param gameDifficulty ゲームの難易度
   * @param entityDataMap  エンティティデータマップ
   */
  private void initGameAtFirstTick(
      Player player, PlayerGameData playerGameData, GameDifficulty gameDifficulty,
      Map<LivingEntity, EntityData> entityDataMap) {

    scoreboardManager.createScoreboard(player);
    spawnEntityManager.spawnInitialCursedEntity(
        player, gameDifficulty, playerGameData, entityDataMap);
    bossBarManager.showBossBarToPlayer(player);
  }

  /**
   * すべてのペアを揃えた場合に処理を実行する。<br>
   * 残り時間をスコアに加算し、残り時間を0にしてゲームを終了する。<br>
   * クリア情報をDBに登録する（初回クリア時のみ）。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   * @param gameDifficulty ゲームの難易度
   * @param entityDataMap  エンティティデータマップ
   * @param timeLeft       ゲームの残り時間
   */
  private void handleFindAllPairs(
      Player player, PlayerGameData playerGameData, GameDifficulty gameDifficulty,
      Map<LivingEntity, EntityData> entityDataMap, int timeLeft) {

    soundEffectManager.playGameClearSound(player);

    playerGameData.setFirstClear(gameStatusChecker.isFirstClear(player));
    playerGameData.addScore((int) Math.round(timeLeft * gameDifficulty.getTimeBonus()));
    playerGameData.endTime();
    playerProgressConnector.updateGameClear(player.getUniqueId(), gameDifficulty);

    // タイトル表示の競合防止としてrunTask()で実行
    Bukkit.getScheduler().runTask(main, () ->
        handleGameEnd(player, playerGameData, gameDifficulty, entityDataMap));
  }

  /**
   * ゲーム終了時に処理を実行する。<br>
   * ボスバーを削除し、ゲーム終了のメッセージとスコアを表示する。<br>
   * DBにスコア情報を登録してプレイヤーの無敵状態を解除し、出現させたエンティティを消滅させる。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーデータ
   * @param gameDifficulty ゲームの難易度
   * @param entityDataMap  エンティティデータマップ
   */
  private void handleGameEnd(
      Player player, PlayerGameData playerGameData,
      GameDifficulty gameDifficulty, Map<LivingEntity, EntityData> entityDataMap) {

    bossBarManager.removeBossBar();

    boolean isClear = playerGameData.isFirstClear() != null;
    boolean isHighScore =
        playerGameData.getScore() > playerScoreConnector.getHighScore(gameDifficulty);

    addEffectIfGameOver(player, isClear);
    showAndRegisterGameResult(player, playerGameData, gameDifficulty, isClear, isHighScore);
    resetGame(player, playerGameData, entityDataMap);
  }

  /**
   * 時間切れでゲームオーバーになった場合は視覚効果をつける。
   *
   * @param player  ゲームを実行中のプレイヤー
   * @param isClear クリアしたかどうか
   */
  private void addEffectIfGameOver(Player player, boolean isClear) {
    if (!isClear) {
      soundEffectManager.playGameOverSound(player);
      visualEffectManager.spawnGameOverParticle(player);
    }
  }

  /**
   * ゲームの結果を表示し、DBにスコアを登録する。<br>
   * ハイスコアを更新した場合は併せてメッセージを表示し、初回クリアの場合は初回クリア用の処理を行う。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   * @param gameDifficulty ゲームの難易度
   * @param isClear        クリアしたかどうか
   * @param isHighScore    ハイスコアを更新したかどうか
   */
  private void showAndRegisterGameResult(
      Player player, PlayerGameData playerGameData, GameDifficulty gameDifficulty,
      boolean isClear, boolean isHighScore) {

    sendTextManager.sendGameEndTitle(player, isClear, playerGameData);

    long delayTime = TickTime.DELAY_3_SECONDS;
    Bukkit.getScheduler().runTaskLater(main, () -> {
      sendTextManager.sendGameScoreTitle(player, playerGameData, isHighScore);
      scoreboardManager.clearScoreboard(player);
      if (isHighScore) {
        visualEffectManager.spawnFireworksForHighScore(player);
      }
    }, delayTime);

    playerScoreConnector.insertNewScore(playerGameData, gameDifficulty);

    if (playerGameData.isFirstClear() != null && playerGameData.isFirstClear().equals(true)) {
      handleIfFirstClear(player, delayTime);
    }
  }

  /**
   * ゲームをリセットする。<br>
   * 出現させたエンティティを消滅させ、プレイヤーの状態をゲーム開始前の状態に戻し、プレイヤーのゲームデータを削除する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   * @param entityDataMap  エンティティデータマップ
   */
  private void resetGame(
      Player player, PlayerGameData playerGameData, Map<LivingEntity, EntityData> entityDataMap) {

    entityDataMap.keySet().forEach(Entity :: remove);
    restorePlayerStatus(player, playerGameData);
    playerGameDataMap.remove(player.getUniqueId());
  }

  /**
   * プレイヤーの無敵状態を解除し、ゲーム実行時にメインハンドに持っていたアイテムを戻す。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   */
  private void restorePlayerStatus(Player player, PlayerGameData playerGameData) {
    player.setInvulnerable(false);
    player.getInventory().setItemInMainHand(playerGameData.getMainHandItem());
  }

  /**
   * 初回クリアの場合、エピローグが書かれた本を開いて5秒後に閉じ、初回クリア時のメッセージをタイトル表示する。
   *
   * @param player    ゲームを実行中のプレイヤー
   * @param delayTime 処理を遅らせる時間
   */
  private void handleIfFirstClear(Player player, long delayTime) {
    delayTime += TickTime.DELAY_5_SECONDS;
    Bukkit.getScheduler().runTaskLater(main, () ->
        writtenBookManager.openEpilogueBook(player), delayTime);

    delayTime += TickTime.DELAY_20_SECONDS; // エピローグを読む時間分遅らせる
    Bukkit.getScheduler().runTaskLater(main, () -> {
      player.closeInventory();
      sendTextManager.sendFirstClearTitle(player);
    }, delayTime);
  }
}
