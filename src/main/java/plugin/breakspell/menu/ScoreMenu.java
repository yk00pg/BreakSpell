package plugin.breakspell.menu;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.breakspell.game.constant.GameDifficulty;

public enum ScoreMenu {

  NEW(
      0, Material.CLOCK,
      "新着スコア", "最近登録されたスコアを最大5件表示する"),
  RANK(
      2, Material.CLOCK,
      "全体ランキング", "全難易度のスコアTOP5を表示する"),
  EASY(
      4, Material.CLOCK,
      "難易度別ランキング（イージー）", "イージーモードのスコアTOP5を表示する"),
  NORMAL(
      5, Material.CLOCK,
      "難易度別ランキング（ノーマル）", "ノーマルモードのスコアTOP5を表示する"),
  HARD(
      6, Material.CLOCK,
      "難易度別ランキング（ハード）", "ハードモードのスコアTOP5を表示する"),
  CLOSE(
      8, Material.WAXED_COPPER_DOOR,
      "閉じる", "スコアメニューを閉じる");

  @Getter
  private final int slotNum;
  private final Material material;
  private final String menuName;
  private final String lore;

  ScoreMenu(int slotNum, Material material, String menuName, String lore) {
    this.slotNum = slotNum;
    this.material = material;
    this.menuName = menuName;
    this.lore = lore;
  }

  /**
   * 素材をアイテムスタックに変換し、表示名を設定して取得する。
   *
   * @return 表示名を設定したアイテム（スタック）
   */
  public ItemStack getItemStack() {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.addItemFlags(ItemFlag.values());
      meta.setDisplayName(ChatColor.GOLD + menuName);
      meta.setLore(List.of(ChatColor.YELLOW + lore));
      item.setItemMeta(meta);
    }
    return item;
  }

  /**
   * 合致するスロット番号を持ったスコアメニューを取得する。
   *
   * @param slotNum スロット番号
   * @return 合致するスロット番号を持ったスコアメニュー
   */
  public static Optional<ScoreMenu> getFilteredScoreMenu(int slotNum) {
    return Arrays.stream(values())
        .filter(gameMenu -> gameMenu.slotNum == slotNum)
        .findFirst();
  }

  /**
   * クリックしたゲームメニューに応じてゲームの難易度を取得する。
   *
   * @param scoreMenu ゲームメニュー
   * @return ゲームの難易度
   */
  public static GameDifficulty getDifficulty(ScoreMenu scoreMenu) {
    return switch (scoreMenu) {
      case EASY -> GameDifficulty.EASY;
      case NORMAL -> GameDifficulty.NORMAL;
      case HARD -> GameDifficulty.HARD;
      default -> throw new IllegalArgumentException("Unexpected score menu: " + scoreMenu);
    };
  }
}