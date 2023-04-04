package github.kasuminova.serverhelper;

import github.kasuminova.serverhelper.command.ServerHelperCommandExecutor;
import github.kasuminova.serverhelper.data.BridgeClientConfig;
import github.kasuminova.serverhelper.extensions.ServerStartCommands;
import github.kasuminova.serverhelper.extensions.WorldFlagForceSetterThread;
import github.kasuminova.serverhelper.extensions.handler.WorldEventHandler;
import github.kasuminova.serverhelper.network.BridgeClient;
import github.kasuminova.serverhelper.util.FileUtils;
import io.netty.util.internal.ThrowableUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class ServerHelperBridge extends JavaPlugin {
    public static final String PROTOCOL_VERSION = "1.0.0";
    public static ServerHelperBridge instance = null;
    public FileConfiguration config = null;
    public Logger logger = null;
    public BridgeClient cl = null;
    public WorldEventHandler worldEventHandler;
    public WorldFlagForceSetterThread flagForceSetterThread;

    @Override
    public void onLoad() {
        instance = this;
        logger = getLogger();
        config = getConfig();
    }

    @Override
    public void onEnable() {
        getCommand("serverhelper").setExecutor(ServerHelperCommandExecutor.INSTANCE);

        File dataFolder = getDataFolder();
        File configFile = new File(dataFolder.getPath() + File.separator + "config.yml");
        try {
            if (!configFile.exists()) {
                if (!dataFolder.exists()) {
                    if (!dataFolder.mkdirs()) {
                        logger.warning(dataFolder.getPath() + " 创建失败！");
                    }
                }
                FileUtils.extractJarFile("/config.yml", configFile.toPath());
            }
            config.load(configFile);
        } catch (Exception e) {
            logger.warning("配置文件加载失败！");
            logger.warning(ThrowableUtil.stackTraceToString(e));
        }

        BridgeClientConfig bridgeClientConfig = new BridgeClientConfig();
        bridgeClientConfig.loadFromConfig(config);
        cl = new BridgeClient(bridgeClientConfig);

        CompletableFuture.runAsync(() -> {
            try {
                cl.connect();
            } catch (Exception e) {
                logger.warning("连接至中心服务器失败！");
                logger.warning(ThrowableUtil.stackTraceToString(e));
            }
        });

        logger.info("注册事件监听器...");
        worldEventHandler = new WorldEventHandler();
        Bukkit.getPluginManager().registerEvents(worldEventHandler, this);

        flagForceSetterThread = new WorldFlagForceSetterThread().loadFromConfig(config).start();

        ServerStartCommands.INSTANCE.loadFormConfig(config);
        CompletableFuture.runAsync(ServerStartCommands.INSTANCE::execute);
    }

    @Override
    public void onDisable() {
        cl.disconnect();

        logger.info("注销事件监听器...");
        HandlerList.unregisterAll(this);

        flagForceSetterThread.interrupt();
    }
}
