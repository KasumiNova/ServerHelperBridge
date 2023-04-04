package github.kasuminova.serverhelper.sender;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

public class SpigotSenderAdapter extends CommandSender.Spigot {
    private final AbstractServerHelperSender parent;

    public SpigotSenderAdapter(AbstractServerHelperSender parent) {
        this.parent = parent;
    }

    @Override
    public void sendMessage(BaseComponent component) {
        parent.sendMessage(component.toLegacyText());
    }

    @Override
    public void sendMessage(BaseComponent... components) {
        parent.sendMessage(BaseComponent.toLegacyText(components));
    }
}
