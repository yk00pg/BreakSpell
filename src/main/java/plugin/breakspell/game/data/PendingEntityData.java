package plugin.breakspell.game.data;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;

/**
 * ペアが揃ったかを判定するまでの間、保留中になっているエンティティの情報を扱うオブジェクト。<br>
 * 呪いをかけられた姿のエンティティとそのエンティティデータの情報を持つ。
 */
@Getter
public class PendingEntityData {

  private final LivingEntity cursedEntity;
  private final EntityData entityData;

  public PendingEntityData(LivingEntity cursedEntity, EntityData entityData) {
    this.cursedEntity = cursedEntity;
    this.entityData = entityData;
  }
}
