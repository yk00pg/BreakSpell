package plugin.breakspell.game.constant;

/**
 * パーティクルやエンティティなどの出現時に使用する位置（オフセット）の定数クラス。
 */
public final class Offsets {

  // X軸
  public static final double LITTLE_EAST = 0.5;
  public static final double LITTLE_WEST = -0.5;

  // Y軸
  public static final double ALMOST_EYE_LEVEL = 1.5;
  public static final double ABOVE = 1.0;
  public static final double LITTLE_ABOVE = 0.5;
  public static final double BIT_FLOAT = 0.3;
  public static final double TINY_BIT_ABOVE = 0.1;

  // Z軸
  public static final double LITTLE_SOUTH = 0.5;
  public static final double TWO_BLOCK_NORTH = -2.0;

  private Offsets() {
    // インスタンス化を防止
  }
}
