package plugin.breakspell.game.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

/**
 * スペシャルエンティティの情報を扱うenum。
 */
@Getter
public enum SpecialEntity {
  ALLAY(EntityType.ALLAY,
      ChatColor.AQUA + "" + ChatColor.BOLD + "アレイ",
      ChatColor.LIGHT_PURPLE + "5秒プラス！"),
  SHULKER(EntityType.SHULKER,
      ChatColor.BLUE + "" + ChatColor.BOLD + "シュルカー",
      ChatColor.RED + "5秒マイナス！"),
  ENDERMAN(EntityType.ENDERMAN,
      ChatColor.GRAY + "" + ChatColor.BOLD + "エンダーマン",
      ChatColor.RED + "エンティティの位置がシャッフル" + ChatColor.RESET + "された！");

  private final EntityType entityType;
  private final String entityName;
  private final String specialEffect;

  SpecialEntity(EntityType entityType, String entityName, String specialEffect) {
    this.entityType = entityType;
    this.entityName = entityName;
    this.specialEffect = specialEffect;
  }

  /**
   * イージーモード用のスペシャルエンティティリストを取得する。
   *
   * @return イージーモード用のスペシャルエンティティリスト
   */
  public static List<EntityType> getEasySpecialEntityList() {
    return List.of(ALLAY.getEntityType());
  }

  /**
   * ノーマルモード用のスペシャルエンティティリストを取得する。
   *
   * @return ノーマルモード用のスペシャルエンティティリスト
   */
  public static List<EntityType> getNormalSpecialEntityList() {
    List<EntityType> normalSpecialEntityList = Arrays.stream(values())
        .limit(2)
        .flatMap(specialEntity ->
            Stream.of(specialEntity.getEntityType(), specialEntity.getEntityType()))
        .collect(Collectors.toList());
    normalSpecialEntityList.add(ENDERMAN.getEntityType());
    return normalSpecialEntityList;
  }

  /**
   * ハードモード用のスペシャルエンティティリストを取得する。
   *
   * @return ハードモード用のスペシャルエンティティリスト
   */
  public static List<EntityType> getHardSpecialEntityList() {
    List<EntityType> hardSpecialEntityList = Arrays.stream(values())
        .limit(2)
        .flatMap(specialEntity ->
            Stream.of(specialEntity.getEntityType(),
                specialEntity.getEntityType(),
                specialEntity.getEntityType()))
        .collect(Collectors.toList());
    hardSpecialEntityList.addAll(Collections.nCopies(2, ENDERMAN.getEntityType()));
    return hardSpecialEntityList;
  }

  /**
   * 合致するエンティティの種類を持つスペシャルエンティティを取得し、存在しなければnullを返す。
   *
   * @param entityType エンティティの種類
   * @return スペシャルエンティティ
   */
  public static SpecialEntity getFilteredSpecialEntity(EntityType entityType) {
    return Arrays.stream(values())
        .filter(specialEntity -> specialEntity.entityType == entityType)
        .findFirst()
        .orElse(null);
  }
}
