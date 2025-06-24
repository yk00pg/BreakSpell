package plugin.breakspell.game.data;

import lombok.Getter;
import org.bukkit.entity.EntityType;

/**
 * ゲーム実行中に出現させたエンティティの情報を扱うオブジェクト。<br>
 * 変身後のエンティティの種類、真の姿の名前の情報を持つ。
 */
@Getter
public class EntityData {

  private final EntityType transformedEntityType;
  private final String trueNatureName;

  public EntityData(EntityType transformedEntityType, String trueNatureName) {
    this.transformedEntityType = transformedEntityType;
    this.trueNatureName = trueNatureName;
  }
}
