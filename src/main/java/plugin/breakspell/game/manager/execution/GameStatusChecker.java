package plugin.breakspell.game.manager.execution;

import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import plugin.breakspell.database.PlayerProgressConnector;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.data.PlayerGameData;

/**
 * プレイヤーの状況を確認するクラス。
 */
public class GameStatusChecker {

  private final PlayerProgressConnector playerProgressConnector;
  private final Map<UUID, PlayerGameData> playerGameDataMap;

  public GameStatusChecker(
      PlayerProgressConnector playerProgressConnector, Map<UUID, PlayerGameData> playerGameDataMap) {

    this.playerProgressConnector = playerProgressConnector;
    this.playerGameDataMap = playerGameDataMap;
  }

  /**
   * プレイヤーデータマップにプレイヤーのUUIDが含まれているかを確認し、ゲームを実行中か判定する。
   *
   * @param player コマンドを実行したプレイヤー
   * @return ゲームを実行中かどうか
   */
  public boolean isExecutingGame(Player player) {
    return playerGameDataMap.containsKey(player.getUniqueId());
  }

  /**
   * ひとつ前の難易度でゲームをプレイしたことがあるか判定する。
   *
   * @param player         コマンドを実行またはゲームメニューGUIをクリックしたプレイヤー
   * @param gameDifficulty ゲームの難易度
   * @return ゲームのプレイ有無（反転）
   */
  public boolean hasNotPlayedPreDifficulty(Player player, GameDifficulty gameDifficulty) {
    UUID uuid = player.getUniqueId();

    GameDifficulty preDifficulty = GameDifficulty.getPreDifficulty(gameDifficulty);
    if (preDifficulty == null) {
      return false;
    }

    if (gameDifficulty == GameDifficulty.NORMAL || gameDifficulty == GameDifficulty.HARD) {
      return playerProgressConnector.hasNotPlayed(uuid, preDifficulty);
    } else {
      return false;
    }
  }

  /**
   * いずれかの難易度でゲームをプレイしたことがあるか判定する。
   *
   * @param player ゲームメニューGUIを開いているプレイヤー、あるいはコマンドまたはゲームメニューGUIをクリックしたプレイヤー
   * @return 初回プレイかどうか
   */
  public boolean isFirstPlay(Player player) {
    return playerProgressConnector.hasNotPlayedAnyDifficulty(player.getUniqueId());
  }

  /**
   * いずれかの難易度でゲームをクリアしたことがあるか判定する。
   *
   * @param player ゲームメニューGUIを開いているプレイヤー、あるいはゲームを実行中のプレイヤー
   * @return 初回クリアかどうか
   */
  public boolean isFirstClear(Player player) {
    return playerProgressConnector.hasNotClearedAnyDifficulty(player.getUniqueId());
  }

  /**
   * 対象のエンティティがゲームで出現させたエンティティかどうかを判定する。
   *
   * @param livingEntity 対象のエンティティ
   * @return ゲームで出現させたエンティティかどうか
   */
  public boolean isGameEntity(LivingEntity livingEntity) {
    return playerGameDataMap.values().stream()
        .map(PlayerGameData :: getEntityDataMap)
        .anyMatch(entityDataMap -> entityDataMap.containsKey(livingEntity));
  }
}
