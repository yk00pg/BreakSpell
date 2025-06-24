package plugin.breakspell.database.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * プレイヤーのスコア情報を扱うオブジェクト。DBに存在するテーブルと連動する。
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerScore {

  private int id;
  private String playerUuid;
  private String playerName;
  private int score;
  private String difficulty;
  private LocalDateTime registeredAt;

  public PlayerScore(String playerUuid, String playerName, int score, String difficulty) {
    this.playerUuid = playerUuid;
    this.playerName = playerName;
    this.score = score;
    this.difficulty = difficulty;
  }
}
