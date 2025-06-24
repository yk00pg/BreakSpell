package plugin.breakspell.game.manager.effect;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * 演出用のプロローグ・エピローグが書かれた本を管理するクラス。
 */
public class WrittenBookManager {

  private final SoundEffectManager soundEffectManager;

  private static final String MAGIC_BOOK_TITLE = "Break the Spell";

  public WrittenBookManager(SoundEffectManager soundEffectManager) {
    this.soundEffectManager = soundEffectManager;
  }

  /**
   * プロローグが書かれた本を開く。
   *
   * @param player ゲームを実行またはゲームメニュGUIのプロローグアイコンをクリックしたプレイヤー
   */
  public void openPrologueBook(Player player) {
    player.openBook(createPrologueBook());
    soundEffectManager.playOpenPrologueBookSound(player);
  }

  /**
   * プロローグが書かれた本を生成する。
   *
   * @return プロローグが書かれた本
   */
  private ItemStack createPrologueBook() {
    ItemStack prologueBook = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta bookMeta = (BookMeta) prologueBook.getItemMeta();
    if (bookMeta != null) {
      bookMeta.setTitle(MAGIC_BOOK_TITLE);
      bookMeta.setAuthor("");

      String page = """
          §6§l§oRevertere!§r
                §8§o— the magic word.
            With a wave of your wand and a chant,
           the cursed return to their true forms.
            But the magic lasts only a little while.
            So match the pairs,
           and let deep bond of friendship
           conquer the curse
           before the magic §7§ofades...
          """;

      bookMeta.setPages(page);
    }
    prologueBook.setItemMeta(bookMeta);
    return prologueBook;
  }

  /**
   * エピローグが書かれた本を開く。
   *
   * @param player ゲームをクリアまたはゲームメニュGUIのエピローグアイコンをクリックしたプレイヤー
   */
  public void openEpilogueBook(Player player) {
    player.openBook(createEpilogueBook());
    soundEffectManager.playOpenEpilogueBookSound(player);
  }

  /**
   * エピローグが書かれた本を生成する。
   *
   * @return エピローグが書かれた本
   */
  private ItemStack createEpilogueBook() {
    ItemStack epilogueBook = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta bookMeta = (BookMeta) epilogueBook.getItemMeta();
    if (bookMeta != null) {
      bookMeta.setTitle(MAGIC_BOOK_TITLE);
      bookMeta.setAuthor("");

      String page = """
           §8§oThe animals were finally free.
           One by one,
           they faded back to
           their own worlds.
           But what truly ended the curse?
           The word? The wand?
           Maybe... just a little.
           Your courage —and their unshakable bond
           —held the real magic.
           That’s what
              §6§l§obroke the spell.
          """;

      bookMeta.setPages(page);
    }
    epilogueBook.setItemMeta(bookMeta);
    return epilogueBook;
  }
}
