package plugin.breakspell.database.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * プレイヤーのゲームの進捗状況を扱うオブジェクト。DBに存在するテーブルと連動する。
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerProgress {

  private String playerUuid;
  private String difficulty;
  private boolean played;
  private boolean cleared;
  private LocalDateTime clearedAt;

  public PlayerProgress(
      String playerUuid, String difficulty, boolean played, boolean cleared, LocalDateTime clearedAt) {

    this.playerUuid = playerUuid;
    this.difficulty = difficulty;
    this.played = played;
    this.cleared = cleared;
    this.clearedAt = clearedAt;
  }
}
