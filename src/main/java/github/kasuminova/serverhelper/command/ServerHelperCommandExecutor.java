package github.kasuminova.serverhelper.command;

import github.kasuminova.serverhelper.ServerHelperBridge;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ServerHelperCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("serverhelper.reload")) {
                    sender.sendMessage(ChatColor.RED + "你没有权限。");
                    break;
                }
                ServerHelperBridge.instance.onDisable();
                ServerHelperBridge.instance.onEnable();
                sender.sendMessage(ChatColor.GREEN + "ServerHelperBridge 重载完成！");
                break;
            case "help":
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private static void sendHelpMessage(CommandSender sender) {
        if (!sender.hasPermission("serverhelper.help")) {
            sender.sendMessage(ChatColor.RED + "你没有权限。");
        } else {
            sender.sendMessage(ChatColor.RED + "用法: /serverhelper reload, /serverhelper help");
        }
    }
}
