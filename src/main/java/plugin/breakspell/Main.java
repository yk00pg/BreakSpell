package plugin.breakspell;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.breakspell.command.ChantingCommand;
import plugin.breakspell.command.ScoreCommand;
import plugin.breakspell.database.PlayerProgressConnector;
import plugin.breakspell.database.PlayerScoreConnector;
import plugin.breakspell.game.data.PlayerGameData;
import plugin.breakspell.game.manager.effect.ScoreboardManager;
import plugin.breakspell.game.manager.effect.SendTextManager;
import plugin.breakspell.game.manager.effect.SoundEffectManager;
import plugin.breakspell.game.manager.effect.VisualEffectManager;
import plugin.breakspell.game.manager.effect.WrittenBookManager;
import plugin.breakspell.game.manager.execution.ComboBonusManager;
import plugin.breakspell.game.manager.execution.GameExecutor;
import plugin.breakspell.game.manager.execution.GameStatusChecker;
import plugin.breakspell.game.manager.execution.SpawnEntityManager;
import plugin.breakspell.listener.InventoryClickListener;
import plugin.breakspell.listener.OperationRestriction;
import plugin.breakspell.listener.PlayerInteractEntityListener;
import plugin.breakspell.menu.MenuGuiManager;

public final class Main extends JavaPlugin {

  @Override
  public void onEnable() {

    SqlSessionFactory sqlSessionFactory;
    Map<UUID, PlayerGameData> playerGameDataMap = new HashMap<>();

    // セッションファクトリーをインスタンス化
    try {
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    // ゲームの演出を担うクラスをインスタンス化
    VisualEffectManager visualEffectManager = new VisualEffectManager(this);
    SoundEffectManager soundEffectManager = new SoundEffectManager(this);
    WrittenBookManager writtenBookManager = new WrittenBookManager(soundEffectManager);
    ScoreboardManager scoreboardManager = new ScoreboardManager();
    ComboBonusManager comboBonusManager = new ComboBonusManager();
    SendTextManager sendTextManager = new SendTextManager();

    // DBと接続するクラスをインスタンス化
    PlayerScoreConnector playerScoreConnector = new PlayerScoreConnector(sqlSessionFactory, sendTextManager);
    PlayerProgressConnector playerProgressConnector = new PlayerProgressConnector(sqlSessionFactory);

    // ゲームの実行を担うクラスをインスタンス化
    GameStatusChecker gameStatusChecker =
        new GameStatusChecker(playerProgressConnector, playerGameDataMap);

    SpawnEntityManager spawnEntityManager = new SpawnEntityManager(visualEffectManager);

    GameExecutor gameExecutor =
        new GameExecutor(
            this, gameStatusChecker, writtenBookManager, visualEffectManager,
            soundEffectManager, sendTextManager, spawnEntityManager, scoreboardManager,
            playerScoreConnector, playerProgressConnector, playerGameDataMap);

    // GUI管理を担うクラスをインスタンス化
    MenuGuiManager menuGuiManager = new MenuGuiManager(this, gameStatusChecker);

    // イベントリスナーを登録
    Bukkit.getPluginManager().registerEvents(
        new InventoryClickListener(
            this, writtenBookManager, gameStatusChecker, gameExecutor,
            menuGuiManager, sendTextManager, playerScoreConnector), this);

    Bukkit.getPluginManager().registerEvents(
        new PlayerInteractEntityListener(
            this, sendTextManager, soundEffectManager, visualEffectManager, spawnEntityManager,
            comboBonusManager, scoreboardManager, playerGameDataMap), this);

    Bukkit.getPluginManager().registerEvents(
        new OperationRestriction(this, gameStatusChecker), this);

    // コマンドを登録
    ChantingCommand chantingCommand =
        new ChantingCommand(gameStatusChecker, sendTextManager, gameExecutor, menuGuiManager);
    getCommand("chanting").setExecutor(chantingCommand);

    ScoreCommand scoreCommand =
        new ScoreCommand(gameStatusChecker, sendTextManager, menuGuiManager, playerScoreConnector);
    getCommand("score").setExecutor(scoreCommand);
  }
}
