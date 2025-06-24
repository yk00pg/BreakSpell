package plugin.breakspell.game.manager.effect;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import plugin.breakspell.Main;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.Pitch;
import plugin.breakspell.game.constant.SpecialEntity;
import plugin.breakspell.game.constant.TickTime;
import plugin.breakspell.game.constant.Volume;

/**
 * 演出用のサウンドを再生・停止するクラス。
 */
public class SoundEffectManager {

  private final Main main;

  public SoundEffectManager(Main main) {
    this.main = main;
  }

  /**
   * プロローグが書かれた本を開く際のサウンドを再生する。
   *
   * @param player プロローグが書かれた本を開いたプレイヤー
   */
  public void playOpenPrologueBookSound(Player player) {
    player.playSound(player, Sound.BLOCK_BARREL_OPEN, Volume.MEDIUM, Pitch.EXTRA_LOW);
    player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, Volume.STANDARD, Pitch.MIDDLE_LOW);
  }

  /**
   * エピローグが書かれた本を開く際のサウンドを再生する。
   *
   * @param player エピローグが書かれた本を開いたプレイヤー
   */
  public void playOpenEpilogueBookSound(Player player) {
    player.playSound(player, Sound.BLOCK_BARREL_CLOSE, Volume.MEDIUM, Pitch.EXTRA_LOW);
    player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, Volume.STANDARD, Pitch.MIDDLE_LOW);
  }

  /**
   * 魔法の本を書見台に置いた際のサウンドを再生する。<br/>
   *
   * @param player 書見台に本を置いたプレイヤー
   */
  public void playMagicBookOnLecternSound(Player player) {
    player.playSound(player, Sound.MUSIC_DISC_FAR, SoundCategory.MUSIC, Volume.EXTRA_LOUD, Pitch.VERY_LOW);
  }

  /**
   * 魔法の本を書見台に置いた際のサウンドを停止する。
   *
   * @param player 書見台に本を置いたプレイヤー
   */
  public void stopMagicBookOnLecternSound(Player player) {
    player.stopSound(Sound.MUSIC_DISC_FAR, SoundCategory.MUSIC);
  }

  /**
   * ゲーム開始前のカウントダウン用のサウンドを再生する。
   *
   * @param player   ゲームを実行中のプレイヤー
   * @param idleTime カウントダウンの残り時間
   */
  public void playCountDownToStartGameSound(Player player, int idleTime) {
    switch (idleTime) {
      case 0 -> {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_FLUTE, Volume.LOUD, Pitch.MEDIUM);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, Volume.LITTLE_QUIET, Pitch.MIDDLE_LOW);
        player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, Volume.STANDARD, Pitch.LITTLE_LOW);
        player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, Volume.LITTLE_QUIET, Pitch.MEDIUM);
      }
      case 1, 2, 3 -> {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HAT, Volume.MEDIUM, Pitch.VERY_LOW);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_FLUTE, Volume.MEDIUM, Pitch.LOW);
      }
      default -> {  // 何も流さない
      }
    }
  }

  /**
   * 呪いをかけられた姿のエンティティ用のサウンドをゲームの難易度に応じて再生する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param gameDifficulty ゲームの難易度
   */
  public void playCursedEntitySound(Player player, GameDifficulty gameDifficulty) {
    switch (gameDifficulty) {
      case EASY -> playZombieAmbient(player);
      case NORMAL -> {
        playZombieAmbient(player);
        playWitherSkeletonAmbient(player);
      }
      case HARD -> {
        playZombieAmbient(player);
        playWitherSkeletonAmbient(player);
        playCreakingAmbient(player);
      }
    }
  }

  /**
   * 近くにゾンビがいる音を再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  private void playZombieAmbient(Player player) {
    player.playSound(player, Sound.ENTITY_ZOMBIE_AMBIENT, Volume.QUIET, Pitch.EXTRA_LOW);
  }

  /**
   * 近くにウィザースケルトンがいる音を再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  private void playWitherSkeletonAmbient(Player player) {
    player.playSound(player, Sound.ENTITY_WITHER_SKELETON_AMBIENT, Volume.QUIET, Pitch.EXTRA_LOW);
  }

  /**
   * 近くにクリーキングがいる音を再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  private void playCreakingAmbient(Player player) {
    player.playSound(player, Sound.ENTITY_CREAKING_AMBIENT, Volume.QUIET, Pitch.EXTRA_LOW);
  }

  /**
   * 魔法の杖を振って魔法を唱える際のサウンドを再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void playWaveMagicWandSound(Player player) {
    player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, Volume.STANDARD, Pitch.MEDIUM);
    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Volume.MEDIUM, Pitch.MEDIUM);

    Sound[] sounds = {
        Sound.BLOCK_NOTE_BLOCK_BELL,
        Sound.BLOCK_NOTE_BLOCK_FLUTE,
        Sound.BLOCK_NOTE_BLOCK_CHIME,
        Sound.BLOCK_NOTE_BLOCK_BELL,
        Sound.BLOCK_NOTE_BLOCK_FLUTE,
        Sound.BLOCK_NOTE_BLOCK_CHIME,
    };

    float[] volumes = {
        Volume.LITTLE_QUIET, Volume.QUIET, Volume.VERY_QUIET,
        Volume.EXTRA_QUIET, Volume.ALMOST_SILENT, Volume.SILENT};
    float[] pitches = {
        Pitch.EXTRA_LOW, Pitch.MIDDLE_LOW, Pitch.LOW, Pitch.MEDIUM, Pitch.LITTLE_HIGH, Pitch.HIGH};

    for (int s = 0; s < sounds.length; s++) {
      final Sound sound = sounds[s];
      final float volume = volumes[s];
      final float pitch = pitches[s];
      Bukkit.getScheduler().runTaskLater(main, () ->
          player.playSound(player, sound, volume, pitch), TickTime.DELAY_BIT * s);
    }
  }

  /**
   * ペアが揃った場合のサウンドを再生する。
   *
   * @param player ゲームを実行中プレイヤー
   */
  public void playMatchPairSound(Player player) {
    long delayTime = TickTime.DELAY_BIT;

    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, Volume.LITTLE_QUIET, Pitch.MIDDLE_LOW);

    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, Volume.LITTLE_QUIET, Pitch.MIDDLE_LOW),
        delayTime);

    delayTime += TickTime.DELAY_BIT;
    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, Volume.LITTLE_QUIET, Pitch.MIDDLE_LOW),
        delayTime);
  }

  /**
   * ペアが揃わなかった場合のサウンドを再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void playMisMatchPairSound(Player player) {
    long delayTime = TickTime.DELAY_BIT;

    player.playSound(player, Sound.ENTITY_WITHER_SPAWN, Volume.SILENT, Pitch.VERY_LOW);

    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.ENTITY_ELDER_GUARDIAN_CURSE, Volume.SILENT, Pitch.VERY_LOW),
        delayTime);

    delayTime += TickTime.DELAY_SHORT;
    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.BLOCK_BREWING_STAND_BREW, Volume.SILENT, Pitch.LOW),
        delayTime);

    delayTime += TickTime.DELAY_BIT;
    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, Volume.SILENT, Pitch.MIDDLE_LOW),
        delayTime);

    delayTime += TickTime.DELAY_BIT;
    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.ENTITY_ELDER_GUARDIAN_CURSE, Volume.SILENT, Pitch.LOWEST),
        delayTime);

    delayTime += TickTime.DELAY_SHORT;
    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, Volume.SILENT, Pitch.EXTRA_LOW),
        delayTime);
  }

  /**
   * スペシャルエンティティの姿に変わった場合のサウンドを再生する。
   *
   * @param player        ゲームを実行中のプレイヤー
   * @param specialEntity スペシャルエンティティ
   */
  public void playRevealSpecialEntitySound(Player player, SpecialEntity specialEntity) {
    switch (specialEntity) {
      case ALLAY -> {
        long delayTime = TickTime.DELAY_BIT;

        player.playSound(player, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, Volume.STANDARD, Pitch.LITTLE_LOW);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, Volume.LITTLE_QUIET, Pitch.MEDIUM);
        player.playSound(player, Sound.ENTITY_WANDERING_TRADER_YES, Volume.LITTLE_QUIET, Pitch.EXTRA_HIGH);

        Bukkit.getScheduler().runTaskLater(main, () -> {
          player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, Volume.QUIET, Pitch.MEDIUM);
          player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, Volume.VERY_QUIET, Pitch.LITTLE_HIGH);
        }, delayTime);

        delayTime += TickTime.DELAY_BIT;
        Bukkit.getScheduler().runTaskLater(main, () -> {
          player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Volume.VERY_QUIET, Pitch.LITTLE_HIGH);
          player.playSound(player, Sound.ENTITY_WANDERING_TRADER_YES, Volume.ALMOST_SILENT, Pitch.HIGHEST);
        }, delayTime);

        delayTime += TickTime.DELAY_BIT;
        Bukkit.getScheduler().runTaskLater(main, () -> {
          player.playSound(player, Sound.ENTITY_PARROT_AMBIENT, Volume.LITTLE_QUIET, Pitch.MEDIUM);
          player.playSound(player, Sound.ENTITY_BAT_TAKEOFF, Volume.EXTRA_QUIET, Pitch.MIDDLE_HIGH);
          player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, Volume.ALMOST_SILENT, Pitch.HIGH);
          player.playSound(player, Sound.ENTITY_WANDERING_TRADER_YES, Volume.VERY_QUIET, Pitch.VERY_HIGH);
        }, delayTime);
      }
      case SHULKER -> {
        player.playSound(player, Sound.ENTITY_SHULKER_AMBIENT, Volume.LITTLE_QUIET, Pitch.EXTRA_LOW);
        player.playSound(player, Sound.BLOCK_PORTAL_AMBIENT, Volume.ALMOST_SILENT, Pitch.VERY_LOW);
        Bukkit.getScheduler().runTaskLater(main, () ->
                player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, Volume.EXTRA_QUIET, Pitch.EXTRA_LOW),
            TickTime.DELAY_MIDDLE);
      }
      case ENDERMAN -> {
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, Volume.MEDIUM, Pitch.MIDDLE_LOW);
        player.playSound(player, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, Volume.EXTRA_QUIET, Pitch.VERY_LOW);
        Bukkit.getScheduler().runTaskLater(main, () ->
                player.playSound(
                    player, Sound.BLOCK_BEACON_DEACTIVATE, Volume.ALMOST_SILENT, Pitch.VERY_LOW),
            TickTime.DELAY_SHORT);
      }
    }
  }

  /**
   * ゲーム終了前のカウントダウン用のサウンドを再生する。
   *
   * @param player   ゲームを実行中のプレイヤー
   * @param gameTime ゲームの残り時間
   */
  public void playCountDownToEndGameSound(Player player, int gameTime) {
    switch (gameTime) {
      case 1 -> {
        player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, Volume.VERY_LOUD, Pitch.HIGH);
        player.playSound(player, Sound.BLOCK_DISPENSER_FAIL, Volume.STANDARD, Pitch.EXTRA_LOW);
      }
      case 2 -> {
        player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, Volume.LOUD, Pitch.LITTLE_HIGH);
        player.playSound(player, Sound.BLOCK_DISPENSER_FAIL, Volume.MEDIUM, Pitch.EXTRA_LOW);
      }
      case 3 -> {
        player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, Volume.LITTLE_LOUD, Pitch.MEDIUM);
        player.playSound(player, Sound.BLOCK_DISPENSER_FAIL, Volume.LITTLE_QUIET, Pitch.EXTRA_LOW);
      }
      case 4, 5 -> {
        player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, Volume.STANDARD, Pitch.MIDDLE_LOW);
        player.playSound(player, Sound.BLOCK_DISPENSER_FAIL, Volume.QUIET, Pitch.EXTRA_LOW);
      }
    }
  }

  /**
   * 制限時間内にゲームをクリアした場合のサウンドを再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void playGameClearSound(Player player) {

    float[] volumes
        = {Volume.MEDIUM, Volume.LITTLE_QUIET, Volume.VERY_QUIET, Volume.ALMOST_SILENT};
    float[] pitches1 = {Pitch.MIDDLE_LOW, Pitch.LOW, Pitch.MEDIUM, Pitch.LITTLE_HIGH};
    float[] pitches2 = {Pitch.MEDIUM, Pitch.LITTLE_HIGH, Pitch.HIGH, Pitch.VERY_HIGH};

    for (int s = 0; s < volumes.length; s++) {
      long delayTime = TickTime.DELAY_LONG * (s + 1);

      final float volume = volumes[s];
      final float pitch1 = pitches1[s];
      final float pitch2 = pitches2[s];

      Bukkit.getScheduler().runTaskLater(main, () -> {
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, volume, pitch1);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_HARP, volume, pitch2);
      }, delayTime);
    }
  }

  /**
   * 制限時間が終了した際のサウンドを再生する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void playGameOverSound(Player player) {
    player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, Volume.LITTLE_QUIET, Pitch.VERY_LOW);
    Bukkit.getScheduler().runTaskLater(main, () ->
            player.playSound(player, Sound.ENTITY_GHAST_SHOOT, Volume.MEDIUM, Pitch.EXTRA_LOW)
        , TickTime.DELAY_LONG);
  }
}
