package plugin.breakspell.game.constant;

/**
 * タスク実行時に使用する時間（tick）の定数クラス。
 */
public final class TickTime {

  // 処理を繰り返す間隔を定数化
  public static final long PERIOD_HALF_SECOND = 10;
  public static final long PERIOD_1_SECOND = 20;

  // 処理を遅延させる時間を定数化
  public static final long DELAY_BIT = 2;
  public static final long DELAY_SHORT = 5;
  public static final long DELAY_MIDDLE = 8;
  public static final long DELAY_LONG = 10;
  public static final long DELAY_1_SECOND = 20;
  public static final long DELAY_2_SECONDS = 2 * DELAY_1_SECOND;
  public static final long DELAY_3_SECONDS = 3 * DELAY_1_SECOND;
  public static final long DELAY_5_SECONDS = 5 * DELAY_1_SECOND;
  public static final long DELAY_20_SECONDS = 20 * DELAY_1_SECOND;

  private TickTime() {
    // インスタンス化を防止
  }
}
