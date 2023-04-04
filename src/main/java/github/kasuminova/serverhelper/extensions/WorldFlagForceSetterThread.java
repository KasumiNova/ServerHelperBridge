package github.kasuminova.serverhelper.extensions;

import github.kasuminova.serverhelper.ServerHelperBridge;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

public class WorldFlagForceSetterThread implements Runnable {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    public final List<String> forceSpawnMonsterWorlds = new ArrayList<>();
    public boolean forceAllWorldSpawnMonsters = false;
    private final Logger logger = ServerHelperBridge.instance.logger;
    private Thread thread = null;

    public WorldFlagForceSetterThread start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.setName("WorldFlagForceSetter-" + THREAD_COUNT.getAndIncrement());
            thread.start();
        }
        return this;
    }

    public void interrupt() {
        if (thread != null) thread.interrupt();
    }

    public WorldFlagForceSetterThread loadFromConfig(FileConfiguration config) {
        forceAllWorldSpawnMonsters = config.getBoolean("ForceAllWorldSpawnMonsters", false);

        forceSpawnMonsterWorlds.clear();
        forceSpawnMonsterWorlds.addAll(config.getStringList("ForceSpawnMonsterWorlds"));

        return this;
    }

    @Override
    public void run() {
        logger.info("规则强制应用线程已启动.");

        while (!Thread.currentThread().isInterrupted()) {
            LockSupport.parkNanos(1000L * 1000 * 1000);
            checkMonsterSpawnFlags();
        }

        logger.info("规则强制应用线程已停止.");
    }

    private void checkMonsterSpawnFlags() {
        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getAllowMonsters()) {
                continue;
            }

            String worldName = world.getName();
            if (!isForceEnabled(worldName)) {
                continue;
            }

            logger.info("世界 " + worldName + " 怪物生成已关闭，强制启用中...");
            world.setSpawnFlags(true, true);
        }
    }

    private boolean isForceEnabled(String worldName) {
        if (forceAllWorldSpawnMonsters) {
            return true;
        }
        return forceSpawnMonsterWorlds.contains(worldName);
    }
}
