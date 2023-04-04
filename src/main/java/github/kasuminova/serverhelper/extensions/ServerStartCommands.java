package github.kasuminova.serverhelper.extensions;

import github.kasuminova.serverhelper.ServerHelperBridge;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ServerStartCommands {
    public static final ServerStartCommands INSTANCE = new ServerStartCommands();
    public final List<String> commandList = new ArrayList<>();

    private ServerStartCommands() {
    }

    public void loadFormConfig(FileConfiguration config) {
        commandList.clear();
        commandList.addAll(config.getStringList("StartCommands"));
    }

    public void execute() {
        if (commandList.isEmpty()) {
            return;
        }

        ServerHelperBridge.instance.logger.info("执行指令中！");
        for (String command : commandList) {
            ServerHelperBridge.instance.logger.info("执行预设中的指令：" + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
