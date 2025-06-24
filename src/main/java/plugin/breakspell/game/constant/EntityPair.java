package plugin.breakspell.game.constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import plugin.breakspell.game.data.EntityData;

/**
 * エンティティのペア情報を扱うenum。
 */
@Getter
public enum EntityPair {

  RABBIT(EntityType.RABBIT, "始まりのウサギ", "終わりのウサギ"),
  COW(EntityType.COW, "始まりのウシ", "終わりのウシ"),
  PIG(EntityType.PIG, "始まりのブタ", "終わりのブタ"),
  CHICKEN(EntityType.CHICKEN, "始まりのニワトリ", "終わりのニワトリ"),
  SHEEP(EntityType.SHEEP, "始まりのヒツジ", "終わりのヒツジ"),
  FOX(EntityType.FOX, "始まりのキツネ", "終わりのキツネ"),
  CAT(EntityType.CAT, "始まりのネコ", "終わりのネコ"),
  TURTLE(EntityType.TURTLE, "始まりのカメ", "終わりのカメ"),
  FROG(EntityType.FROG, "始まりのカエル", "終わりのカエル"),
  DONKEY(EntityType.DONKEY, "始まりのロバ", "終わりのロバ"),
  CAMEL(EntityType.CAMEL, "始まりのラクダ", "終わりのラクダ"),
  OCELOT(EntityType.OCELOT, "始まりのヤマネコ", "終わりのヤマネコ"),
  HORSE(EntityType.HORSE, "始まりのウマ", "終わりのウマ"),
  MULE(EntityType.MULE, "始まりのラバ", "終わりのラバ"),
  ARMADILLO(EntityType.ARMADILLO, "始まりのアルマジロ", "終わりのアルマジロ"),
  POLAR_BEAR(EntityType.POLAR_BEAR, "始まりのシロクマ", "終わりのシロクマ"),
  PANDA(EntityType.PANDA, "始まりのパンダ", "終わりのパンダ"),
  GOAT(EntityType.GOAT, "始まりのヤギ", "終わりのヤギ"),
  WOLF(EntityType.WOLF, "始まりのオオカミ", "終わりのオオカミ"),
  LLAMA(EntityType.LLAMA, "始まりのラマ", "終わりのラマ");

  private final EntityType entityType;
  private final String firstEntityName;
  private final String lastEntityName;

  EntityPair(EntityType entityType, String firstEntityName, String lastEntityName) {
    this.entityType = entityType;
    this.firstEntityName = firstEntityName;
    this.lastEntityName = lastEntityName;
  }

  /**
   * 指定の難易度のペアの数に応じて、エンティティペアリストを取得する。
   *
   * @param pairNum ペアの数
   * @return エンティティペアリスト
   */
  public static List<EntityType> getEntityPairList(int pairNum) {
    return Arrays.stream(values())
        .limit(pairNum)
        .flatMap(entityPair ->
            Stream.of(entityPair.getEntityType(), entityPair.getEntityType()))
        .collect(Collectors.toList());
  }

  /**
   * エンティティの種類からどのペアかを判定し、ペアのエンティティの名前を取得する。
   *
   * @param entityType エンティティの種類
   * @param entityName エンティティの名前
   * @return ペアのエンティティの名前
   */
  public static String getPairEntityName(EntityType entityType, String entityName) {
    EntityPair entityPair = getFilteredEntityPair(entityType);
    if (entityPair == null) {
      return null;
    }

    if (entityName.equals(entityPair.getFirstEntityName())) {
      return entityPair.getLastEntityName();
    } else {
      return entityPair.getFirstEntityName();
    }
  }

  /**
   * エンティティの名前を取得する。<br>
   * すでに同じ種類のエンティティがリストに含まれていれば2体目のエンティティの名前を、
   * そうでなければ1体目のエンティティの名前を返す。
   *
   * @param entityType    エンティティの種類
   * @param entityDataMap エンティティデータマップ
   * @return エンティティの名前
   */
  public static String getEntityName(
      EntityType entityType, Map<LivingEntity, EntityData> entityDataMap) {

    EntityPair entityPair = getFilteredEntityPair(entityType);
    if (entityPair == null) {
      return null;
    }

    boolean exists =
        entityDataMap.values().stream()
            .anyMatch(entityData -> entityData.getTransformedEntityType() == (entityType));
    if (exists) {
      return entityPair.getLastEntityName();
    } else {
      return entityPair.getFirstEntityName();
    }
  }

  /**
   * 合致するエンティティの種類を持ったエンティティペアを取得し、存在しなければnullを返す。
   *
   * @param entityType エンティティの種類
   * @return エンティティペア
   */
  private static EntityPair getFilteredEntityPair(EntityType entityType) {
    return Arrays.stream(values())
        .filter(entityPair -> entityPair.entityType == entityType)
        .findFirst()
        .orElse(null);
  }
}
