package plugin.breakspell.game.manager.execution;

import plugin.breakspell.game.constant.GameDifficulty;

/**
 * コンボボーナス（連続でペアが揃った場合のボーナス）を管理するクラス。
 */
public class ComboBonusManager {

  /**
   * 連続ペア数に応じたメッセージを取得する。
   *
   * @param consecutivePairs 連続ペア数
   * @return 連続ペア数に応じたメッセージ
   */
  public String getComboMessage(int consecutivePairs) {
    return switch (consecutivePairs) {
      case 1 -> "reunion!";
      case 2 -> "great matching!";
      case 3 -> "unbelievable!";
      case 4 -> "awesome!";
      default -> "clairvoyance?!";
    };
  }

  /**
   * コンボボーナスの倍率を取得する。
   *
   * @param consecutivePairs 連続ペア数
   * @param gameDifficulty   ゲームの難易度
   * @return コンボボーナスの倍率
   */
  public double getComboBonus(int consecutivePairs, GameDifficulty gameDifficulty) {
    return switch (gameDifficulty) {
      case NORMAL -> getNormalComboBonus(consecutivePairs);
      case HARD -> getHardComboBonus(consecutivePairs);
      default -> 1.0;
    };
  }

  /**
   * ノーマルモードのコンボボーナスの倍率を取得する。
   *
   * @param consecutivePairs 連続ペア数
   * @return ノーマルモードのコンボボーナスの倍率
   */
  private double getNormalComboBonus(int consecutivePairs) {
    return switch (consecutivePairs) {
      case 1 -> 1.0;
      case 2 -> 1.5;
      case 3 -> 2.0;
      case 4 -> 2.5;
      default -> 3.0;
    };
  }

  /**
   * ハードモードのコンボボーナスの倍率を取得する。
   *
   * @param consecutivePairs 連続ペア数
   * @return ハードモードのコンボボーナスの倍率
   */
  private double getHardComboBonus(int consecutivePairs) {
    return switch (consecutivePairs) {
      case 1 -> 1.0;
      case 2 -> 2.0;
      case 3 -> 3.0;
      case 4 -> 4.0;
      default -> 5.0;
    };
  }
}
