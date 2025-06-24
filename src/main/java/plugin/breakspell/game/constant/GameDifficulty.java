package plugin.breakspell.game.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.EntityType;

/**
 * ゲームの難易度に関する情報を扱うenum。
 */
@Getter
public enum GameDifficulty {
  EASY("easy", "イージー",
      5, 5, 1.0,
      List.of(EntityType.ZOMBIE),
      getTrueNatureEntityList(5),
      getEasyXZList(),
      30),

  NORMAL("normal", "ノーマル",
      10, 10, 1.5,
      List.of(EntityType.ZOMBIE, EntityType.WITHER_SKELETON),
      getTrueNatureEntityList(10),
      getNormalXZList(),
      90),

  HARD("hard", "ハード",
      20, 20, 2.0,
      List.of(EntityType.ZOMBIE, EntityType.WITHER_SKELETON, EntityType.CREAKING),
      getTrueNatureEntityList(20),
      getHardXZList(),
      180);

  private final String inputArg;
  private final String label;
  private final int pairNum;
  private final int point;
  private final double timeBonus;
  private final List<EntityType> cursedEntityList;
  private final List<EntityType> trueNatureList;
  private final List<List<Integer>> xZList;
  private final int gameTime;

  GameDifficulty(
      String inputArg, String label, int pairNum, int point, double timeBonus,
      List<EntityType> cursedEntityList, List<EntityType> trueNatureList,
      List<List<Integer>> xZList, int gameTime) {

    this.inputArg = inputArg;
    this.label = label;
    this.pairNum = pairNum;
    this.point = point;
    this.timeBonus = timeBonus;
    this.cursedEntityList = cursedEntityList;
    this.trueNatureList = trueNatureList;
    this.xZList = xZList;
    this.gameTime = gameTime;
  }

  /**
   * コマンド引数と合致する難易度を持つゲームモードを取得し、存在しなければnullを返す。
   *
   * @param args コマンド引数
   * @return ゲームモード
   */
  public static GameDifficulty getGameDifficulty(String args) {
    return Arrays.stream(values())
        .filter(gameDifficulty -> gameDifficulty.inputArg.equals(args))
        .findFirst()
        .orElse(null);
  }

  /**
   * 真の姿のエンティティリストを取得する。<br>
   * エンティティペアリストを取得し、ペア数が5のときはイージーモード用、
   * 10のときはノーマルモード用、20のときはハードモード用のスペシャルエンティティリストを追加して返す。
   *
   * @return 真の姿のエンティティリスト
   */
  private static List<EntityType> getTrueNatureEntityList(int pairNum) {
    List<EntityType> trueNatureList = EntityPair.getEntityPairList(pairNum);
    switch (pairNum) {
      case 5 -> trueNatureList.addAll(SpecialEntity.getEasySpecialEntityList());
      case 10 -> trueNatureList.addAll(SpecialEntity.getNormalSpecialEntityList());
      case 20 -> trueNatureList.addAll(SpecialEntity.getHardSpecialEntityList());
    }
    return trueNatureList;
  }

  /**
   * イージーモード用のx軸とz軸の組み合わせリストを取得する。
   *
   * @return イージーモード用のx軸とz軸の組み合わせリスト
   */
  private static List<List<Integer>> getEasyXZList() {
    List<List<Integer>> xzList = new ArrayList<>();
    List<Integer> xList =
        new ArrayList<>(List.of(-7, -6, -4, -3, -1, 0, 2, 3, 5, 6, 8));

    for (int x : xList) {
      int z = x % 3 == 0 ? -6 : -3;
      xzList.add(List.of(x, z));
    }
    return xzList;
  }

  /**
   * ノーマルモード用のx軸とz軸の組み合わせリストを取得する。
   *
   * @return ノーマルモード用のx軸とz軸の組み合わせリスト
   */
  private static List<List<Integer>> getNormalXZList() {
    List<List<Integer>> xzList = new ArrayList<>();

    for (int x = -10; x <= 10; x += 3) {
      for (int z = -3; z >= -23; z -= 3) {
        xzList.add(List.of(x, z));
      }
    }
    Collections.shuffle(xzList);
    return xzList;
  }

  /**
   * ハードモード用のx軸とz軸の組み合わせリストを取得する。
   *
   * @return ハードモード用のx軸とz軸の組み合わせリスト
   */
  private static List<List<Integer>> getHardXZList() {
    List<List<Integer>> xzList = new ArrayList<>();

    for (int x = -15; x <= 15; x += 3) {
      for (int z = -3; z >= -33; z -= 3) {
        xzList.add(List.of(x, z));
      }
    }
    Collections.shuffle(xzList);
    return xzList;
  }

  /**
   * ひとつ前のゲームの難易度を取得し、存在しない場合はnullを返す。
   *
   * @param gameDifficulty ゲームの難易度
   * @return ひとつ前のゲームの難易度
   */
  public static GameDifficulty getPreDifficulty(GameDifficulty gameDifficulty) {
    return switch (gameDifficulty) {
      case NORMAL -> EASY;
      case HARD -> NORMAL;
      default -> null;
    };
  }
}