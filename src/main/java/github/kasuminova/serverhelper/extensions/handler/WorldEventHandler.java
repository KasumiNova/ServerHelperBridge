package github.kasuminova.serverhelper.extensions.handler;

import github.kasuminova.serverhelper.ServerHelperBridge;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.logging.Logger;

public class WorldEventHandler implements Listener {
    private final Logger logger = ServerHelperBridge.instance.logger;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        logger.info("世界 " + world.getName() + " 已加载！");
        logger.info("Environment: " + world.getEnvironment().name());
    }

}
