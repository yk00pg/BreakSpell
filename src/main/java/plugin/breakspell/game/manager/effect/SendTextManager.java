package plugin.breakspell.game.manager.effect;

import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import plugin.breakspell.game.constant.GameDifficulty;
import plugin.breakspell.game.constant.SpecialEntity;
import plugin.breakspell.game.data.PlayerGameData;

/**
 * メッセージやタイトルをプレイヤーに表示するクラス。
 */
public class SendTextManager {

  // 共通するエラーメッセージとその引数を定数化
  private static final String ERROR_INPUT_ARGUMENT_1 = "引数の入力が正しくありません。引数なしで%sを開くか、";
  private static final String GAME_MENU = "ゲームメニュー";
  private static final String SCORE_MENU = "スコアメニュー";

  private static final String ERROR_INPUT_ARGUMENT_2 = "正しい引数を入力してください。 %s";
  private static final String GAME_MENU_ARGS = "[easy, normal, hard]";
  private static final String SCORE_MENU_ARGS = "[new, rank, easy, normal, hard]";

  private static final String NOT_PLAYED_ALERT_1 = "まだゲームを%sしていません。";
  private static final String NOT_PLAYED_ALERT_2 = "いずれかの難易度で%sすると%sを読めるようになります。";
  private static final String PLAY = "プレイ";
  private static final String CLEAR = "クリア";
  private static final String PROLOGUE = "プロローグ";
  private static final String EPILOGUE = "エピローグ";

  // タイトル表示に関する時間（tick）を定数化
  private static final int FADE_TIME = 10;
  private static final int STAY_SHORT = 20;
  private static final int STAY_MIDDLE = 40;
  private static final int STAY_LONG = 60;

  /**
   * ゲームを実行中にコマンドを実行した際のメッセージを表示する。
   *
   * @param player コマンドを実行したプレイヤー
   */
  public void sendDuplicationAlert(Player player) {
    player.sendMessage(ChatColor.RED + "現在ゲームを実行中です。終了してから再度実行してください。");
  }

  /**
   * ゲームを中止した場合のメッセージを表示する。
   *
   * @param player コマンドを実行したプレイヤー
   */
  public void sendStopGameMessage(Player player) {
    player.sendMessage(ChatColor.RED + "stopコマンドが実行されたため、ゲームを中止しました。");
  }

  /**
   * chantingコマンドの引数の入力が間違っていた場合のメッセージを表示する。
   *
   * @param player コマンドを実行したプレイヤー
   */
  public void sendInputErrorAlertForChanting(Player player) {
    getInputAlertLines(GAME_MENU, GAME_MENU_ARGS).forEach(player :: sendMessage);
  }

  /**
   * scoreコマンドの引数の入力が間違っていた場合のメッセージを表示する。
   *
   * @param player コマンドを実行したプレイヤー
   */
  public void sendInputAlertForScoreList(Player player) {
    getInputAlertLines(SCORE_MENU, SCORE_MENU_ARGS).forEach(player :: sendMessage);
  }

  /**
   * コマンドの引数の入力が間違っていた場合に表示するメッセージを取得する。
   *
   * @param menu メニューの名前
   * @param args 正しい引数
   * @return コマンドの引数の入力が間違っていた場合に表示するメッセージ
   */
  private List<String> getInputAlertLines(String menu, String args) {
    List<String> inputAlertLines = new ArrayList<>();
    inputAlertLines.add(ChatColor.RED + String.format(ERROR_INPUT_ARGUMENT_1, menu));
    inputAlertLines.add(ChatColor.RED + String.format(ERROR_INPUT_ARGUMENT_2, args));
    return inputAlertLines;
  }

  /**
   * DBにスコア情報が登録されていない場合のメッセージを表示する。
   *
   * @param player コマンドを実行またはメニューをクリックしたプレイヤー
   */
  public void sendNotExitsScoreListMessage(Player player) {
    player.sendMessage(ChatColor.YELLOW + "まだスコアが登録されていません。");
  }

  /**
   * 整形済みのスコアリストをメッセージとして表示する。
   *
   * @param player     コマンドを実行またはメニューをクリックしたプレイヤー
   * @param scoreLines 整形済みのスコアリスト
   */
  public void sendScoreLines(Player player, List<String> scoreLines) {
    scoreLines.forEach(player :: sendMessage);
  }

  /**
   * ひとつ前の難易度を未プレイの状態でゲームを実行しようとした際のメッセージを表示する。
   *
   * @param player コマンドを実行またはゲームメニューGUIをクリックしたプレイヤー
   */
  public void sendHasNotPlayedPreDifficultyAlert(Player player, GameDifficulty gameDifficulty) {
    GameDifficulty preDifficulty = GameDifficulty.getPreDifficulty(gameDifficulty);
    if (preDifficulty == null) {
      return;
    }
    String previousDifficulty = preDifficulty.getLabel() + "モード";
    String selectedDifficulty = gameDifficulty.getLabel() + "モード";

    player.sendMessage(ChatColor.YELLOW + "まだ" + previousDifficulty + "に挑戦していません。");
    player.sendMessage(ChatColor.YELLOW + "まずは" + previousDifficulty + "をプレイしてから、"
        + selectedDifficulty + "に挑戦しましょう！");
  }

  /**
   * 未プレイの状態でプロローグが書かれた本を開こうとした場合のメッセージを表示する。
   *
   * @param player プロローグが書かれた本を開こうとしたプレイヤー
   */
  public void sendCannotOpenPrologueAlert(Player player) {
    getCannotOpenBookMessage(PLAY, PROLOGUE).forEach(player :: sendMessage);
  }

  /**
   * 未クリアの状態でエピローグが書かれた本を開こうとした場合のメッセージを表示する。
   *
   * @param player エピローグが書かれた本を開こうとしたプレイヤー
   */
  public void sendCannotOpenEpilogueAlert(Player player) {
    getCannotOpenBookMessage(CLEAR, EPILOGUE).forEach(player :: sendMessage);
  }

  /**
   * 本の閲覧エラーメッセージを取得する。
   *
   * @param book プロローグまたはエピローグ
   * @return 本の閲覧エラーメッセージ
   */
  private List<String> getCannotOpenBookMessage(String progress, String book) {
    List<String> openAlertLines = new ArrayList<>();
    openAlertLines.add(ChatColor.YELLOW + String.format(NOT_PLAYED_ALERT_1, progress));
    openAlertLines.add(ChatColor.YELLOW + String.format(NOT_PLAYED_ALERT_2, progress, book));
    return openAlertLines;
  }

  /**
   * ゲームの開始準備を促すメッセージを表示する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void sendStandByMessage(Player player) {
    player.sendMessage(ChatColor.GOLD + "魔法の本"
        + ChatColor.RESET + "を書見台に設置し、ゲームを開始してください。");
  }

  /**
   * ゲームが自動キャンセルされるまでの時間をアクションバーに表示する。
   *
   * @param player      ゲームを実行中のプレイヤー
   * @param pendingTime 自動キャンセルされるまでの時間
   */
  public void sendPendingTimeActionBar(Player player, double[] pendingTime) {
    int time = (int) (Math.round(pendingTime[0]));
    player.spigot().sendMessage(
        ChatMessageType.ACTION_BAR, new TextComponent("自動キャンセルまで残り" + time + "秒"));
  }

  /**
   * ゲームが自動キャンセルされた場合のメッセージを表示する。
   *
   * @param player ゲームを実行していたプレイヤー
   */
  public void sendCancelGameMessage(Player player) {
    player.sendMessage(ChatColor.RED + "魔法の本の効力が消えたため、ゲームをキャンセルしました。");
  }

  /**
   * ゲーム開始までのカウントダウンをタイトル表示する。
   *
   * @param player   ゲームを実行中のプレイヤー
   * @param idleTime ゲーム開始までの待機時間
   */
  public void sendCountDownToStartGameTitle(Player player, int idleTime) {
    switch (idleTime) {
      case 0 -> player.sendTitle(
          ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.ITALIC + "Go!",
          "", FADE_TIME, STAY_SHORT, FADE_TIME);
      case 1 -> player.sendTitle(
          ChatColor.GOLD + "" + ChatColor.BOLD + idleTime,
          "仲間を見つけて絆の力で解放しよう！", FADE_TIME, STAY_SHORT, FADE_TIME);
      case 2 -> player.sendTitle(
          ChatColor.GOLD + "" + ChatColor.BOLD + idleTime,
          "魔法を唱えて動物たちを真の姿に戻し、", FADE_TIME, STAY_SHORT, FADE_TIME);
      case 3 -> player.sendTitle(
          ChatColor.GOLD + "" + ChatColor.BOLD + idleTime,
          "エンティティを右クリックして", FADE_TIME, STAY_SHORT, FADE_TIME);
      case 5 -> player.sendTitle(
          ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.ITALIC + "READY?",
          "", FADE_TIME, STAY_MIDDLE, FADE_TIME);
    }
  }

  /**
   * ゲーム終了までのカウントダウンをタイトル表示する。
   *
   * @param player   ゲームを実行中のプレイヤー
   * @param timeLeft 残り時間
   */
  public void sendCountDownToEndGameTitle(Player player, int timeLeft) {
    player.sendTitle(
        ChatColor.GOLD + "" + ChatColor.BOLD + timeLeft,
        "", FADE_TIME, STAY_SHORT, FADE_TIME);
  }

  /**
   * ゲームが終了した際のメッセージをタイトル表示する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param isClear        クリアしたかどうか
   * @param playerGameData プレイヤーのゲームデータ
   */
  public void sendGameEndTitle(
      Player player, boolean isClear, PlayerGameData playerGameData) {

    String endWord = isClear ? "GAME CLEAR!" : "GAME OVER!";
    player.sendTitle(
        ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.ITALIC + endWord,
        "呪いから解き放たれた動物 : "
            + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + playerGameData.getMatchedPair()
            + ChatColor.RESET + "種",
        FADE_TIME, STAY_LONG, FADE_TIME);
  }

  /**
   * ゲームスコアをタイトル表示する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param playerGameData プレイヤーのゲームデータ
   * @param isHighScore    ハイスコアかどうか
   */
  public void sendGameScoreTitle(
      Player player, PlayerGameData playerGameData, boolean isHighScore) {

    String highScoreMessage = isHighScore ? "ハイスコア更新！" : "";
    player.sendTitle(
        "合計 : "
            + ChatColor.GOLD + ChatColor.BOLD + ChatColor.ITALIC + playerGameData.getScore()
            + ChatColor.RESET + "点",
        ChatColor.GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + highScoreMessage,
        FADE_TIME, STAY_LONG, FADE_TIME);
  }

  /**
   * 初回クリア時のメッセージをタイトル表示する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void sendFirstClearTitle(Player player) {
    player.sendTitle(
        ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "Happily Ever After",
        "— FIN —",
        FADE_TIME, STAY_LONG, FADE_TIME);
  }

  /**
   * 魔法の言葉を表示する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void sendMagicWordMassage(Player player) {
    player.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "'— Revertere!'");
  }

  /**
   * スペシャルエンティティが姿を現した際のメッセージを表示する。
   *
   * @param player        ゲームを実行中のプレイヤー
   * @param specialEntity スペシャルエンティティ
   */
  public void sendRevealSpecialEntityMessage(Player player, SpecialEntity specialEntity) {
    player.sendMessage(
        specialEntity.getEntityName()
            + ChatColor.RESET + "が姿を現した！"
            + specialEntity.getSpecialEffect());
  }

  /**
   * 真の姿のエンティティが姿を現した際のメッセージを表示する。
   *
   * @param player         ゲームを実行中のプレイヤー
   * @param trueNatureName 真の姿のエンティティの名前
   */
  public void sendRevealTrueNatureMessage(Player player, String trueNatureName) {
    player.sendMessage(ChatColor.GREEN + trueNatureName + ChatColor.RESET + "の姿に戻った！");
  }

  /**
   * 1体目をクリックした際のメッセージを表示する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void sendFirstClickMessage(Player player) {
    player.sendMessage(
        "仲間を見つけて"
            + ChatColor.LIGHT_PURPLE + "絆の力"
            + ChatColor.RESET + "で解放しよう！");
  }

  /**
   * ペアが揃った場合のメッセージを表示する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void sendMatchPairMessage(Player player) {
    player.sendMessage(
        ChatColor.DARK_AQUA + "" + ChatColor.BOLD + ChatColor.ITALIC + "「始まり」"
            + ChatColor.RESET + "と"
            + ChatColor.DARK_AQUA + ChatColor.BOLD + ChatColor.ITALIC + "「終わり」"
            + ChatColor.RESET + "の力が合わさり、呪いから解き放たれた！");
  }

  /**
   * ペアが揃わなかった場合のメッセージを表示する。
   *
   * @param player ゲームを実行中のプレイヤー
   */
  public void sendMisMatchMessage(Player player) {
    player.sendMessage("ざんねん！　" + ChatColor.RED + "魔法の効果が切れてしまった！");
  }
}
