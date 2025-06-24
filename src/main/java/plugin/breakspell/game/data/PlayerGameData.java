package plugin.breakspell.game.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import plugin.breakspell.game.constant.GameDifficulty;

/**
 * ゲームを実行中のプレイヤーのゲームデータを扱うオブジェクト。<br>
 * プレイヤーのUUID、名前、プレイ中の難易度、メインハンドに持っていたアイテムなどの情報を持つ。
 */
@Getter
public class PlayerGameData {

  private final UUID playerUuid;
  private final String playerName;
  private final GameDifficulty gameDifficulty;
  private final ItemStack mainHandItem;
  private int score;
  private int matchedPair;
  private int consecutivePairs;
  @Setter
  private int gameTime;
  @Setter
  private boolean spawnedEntity;
  private long clickedTime;
  @Setter
  private Boolean firstClear = null;

  private final Map<LivingEntity, EntityData> entityDataMap = new HashMap<>();
  private final Map<LivingEntity, PendingEntityData> awaitingPairMap = new LinkedHashMap<>();

  public PlayerGameData(
      UUID playerUuid, String playerName, GameDifficulty gameDifficulty, ItemStack mainHandItem) {

    this.playerUuid = playerUuid;
    this.playerName = playerName;
    this.gameDifficulty = gameDifficulty;
    this.mainHandItem = mainHandItem;
  }

  /**
   * スコアを加算する。
   *
   * @param point 加算するポイント
   */
  public void addScore(int point) {
    this.score += point;
  }

  /**
   * マッチしたペア数を追加する。
   */
  public void addMatchedPair() {
    this.matchedPair += 1;
  }

  /**
   * 連続ペア数を更新する。
   */
  public void updateConsecutivePairs() {
    this.consecutivePairs += 1;
  }

  /**
   * 連続ペア数をリセットする。
   */
  public void resetConsecutivePairs() {
    this.consecutivePairs = 0;
  }

  /**
   * 制限時間を追加する。
   *
   * @param time 追加する時間（秒）
   */
  public void addTime(int time) {
    this.gameTime += time;
  }

  /**
   * 制限時間を減らす。
   *
   * @param time 減らす時間（秒）
   */
  public void reduceTime(int time) {
    this.gameTime -= time;
  }

  /**
   * 制限時間を0にする。
   */
  public void endTime() {
    this.gameTime = 0;
  }

  /**
   * エンティティをクリックした時間を更新する。
   *
   * @param time エンティティをクリックした時間（ミリ秒（System.currentTimeMillis()で取得））
   */
  public void updateClickedTime(long time) {
    this.clickedTime = time;
  }

  /**
   * 初回クリアかどうかを取得する。
   *
   * @return 初回クリアかどうか
   */
  public Boolean isFirstClear() {
    return this.firstClear;
  }
}
