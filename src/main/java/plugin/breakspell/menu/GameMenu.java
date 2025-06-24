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

/**
 * ゲームメニューを扱うenum。
 */
public enum GameMenu {
  PROLOGUE(0, Material.WRITTEN_BOOK,
      "プロローグ",
      "プロローグが書かれた本を開く",
      "イージーモードをプレイ後に解放"),
  EASY(2, Material.ZOMBIE_HEAD,
      "EASY MODE",
      "イージーモードでゲームを開始する",
      ""),
  NORMAL(3, Material.WITHER_SKELETON_SKULL,
      "NORMAL MODE",
      "ノーマルモードでゲームを開始します",
      "イージーモードをプレイ後に解放"),
  HARD(4, Material.CREAKING_HEART,
      "HARD MODE",
      "ハードモードでゲームを開始します",
      "ノーマルモードをプレイ後に解放"),
  EPILOGUE(6, Material.WRITTEN_BOOK,
      "エピローグ",
      "エピローグが書かれた本を開く",
      "いずれかの難易度でゲームクリア後に解放"),
  CLOSE(8, Material.WAXED_COPPER_DOOR,
      "閉じる",
      "ゲームメニューを閉じる",
      "");

  @Getter
  private final int slotNum;
  private final Material material;
  private final String menuName;
  private final String lore;
  private final String fallbackLore;

  GameMenu(int slotNum, Material material, String menuName, String lore, String fallbackLore) {
    this.slotNum = slotNum;
    this.material = material;
    this.menuName = menuName;
    this.lore = lore;
    this.fallbackLore = fallbackLore;
  }

  /**
   * メニューの項目名を取得する。<br>
   * 無効の場合は取り消し線をつけてダークグレイで、そうでない場合は金色で文字に色をつける。
   *
   * @param disabled 無効の場合
   * @return 文字の色をつけたメニューの項目名
   */
  private String getMenuName(boolean disabled) {
    return disabled
        ? ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + menuName
        : ChatColor.GOLD + menuName;
  }

  /**
   * メニューの説明文を取得する。<br>
   * 無効の場合はダークグレイで差し替え用の説明文を、そうでない場合は黄色で説明文を取得する。
   *
   * @param disabled 無効の場合
   * @return 文字の色をつけたメニューの説明文
   */
  private List<String> getLore(boolean disabled) {
    return List.of(disabled ? ChatColor.DARK_GRAY + fallbackLore : ChatColor.YELLOW + lore);
  }

  /**
   * 素材をアイテムスタックに変換し、表示名と説明文を設定して取得する。
   *
   * @param disabled 無効の場合
   * @return 表示名と説明文を設定したアイテム（スタック）
   */
  public ItemStack getItemStack(boolean disabled) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.addItemFlags(ItemFlag.values());
      meta.setDisplayName(getMenuName(disabled));
      meta.setLore(getLore(disabled));
      item.setItemMeta(meta);
    }
    return item;
  }

  /**
   * 合致するスロット番号を持ったゲームメニューを取得する。
   *
   * @param slotNum スロット番号
   * @return 合致するスロット番号を持ったゲームメニュー
   */
  public static Optional<GameMenu> getFilteredGameMenu(int slotNum) {
    return Arrays.stream(values())
        .filter(gameMenu -> gameMenu.slotNum == slotNum)
        .findFirst();
  }

  /**
   * クリックしたゲームメニューに応じてゲームの難易度を取得する。
   *
   * @param gameMenu ゲームメニュー
   * @return ゲームの難易度
   */
  public static GameDifficulty getDifficulty(GameMenu gameMenu) {
    switch (gameMenu) {
      case EASY -> {
        return GameDifficulty.EASY;
      }
      case NORMAL -> {
        return GameDifficulty.NORMAL;
      }
      case HARD -> {
        return GameDifficulty.HARD;
      }
    }
    return null;
  }
}
