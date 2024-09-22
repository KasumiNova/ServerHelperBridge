package github.kasuminova.serverhelper.network.handler;

import github.kasuminova.network.message.playercmd.PlayerCmdExecFailedMessage;
import github.kasuminova.network.message.playercmd.PlayerCmdExecMessage;
import github.kasuminova.network.message.protocol.ClientType;
import github.kasuminova.network.message.protocol.ClientTypeMessage;
import github.kasuminova.network.message.protocol.HeartbeatResponse;
import github.kasuminova.network.message.protocol.PreDisconnectMessage;
import github.kasuminova.network.message.servercmd.CmdExecFailedMessage;
import github.kasuminova.network.message.servercmd.CmdExecMessage;
import github.kasuminova.network.message.serverinfo.*;
import github.kasuminova.serverhelper.ServerHelperBridge;
import github.kasuminova.serverhelper.network.BridgeClient;
import github.kasuminova.serverhelper.data.BridgeClientConfig;
import github.kasuminova.serverhelper.sender.ServerHelperSender;
import io.netty.util.internal.ThrowableUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainHandler extends AbstractHandler<MainHandler> {

    private final BridgeClient cl;

    public MainHandler(BridgeClient cl) {
        this.cl = cl;
    }

    @Override
    protected void onRegisterMessages() {
        registerMessage(CmdExecMessage.class, (handler, message) -> {
            String senderName = message.sender;
            String cmd = message.cmd.replace("\\", "");

            if (senderName.isEmpty()) {
                try {
                    Bukkit.getScheduler().runTask(ServerHelperBridge.instance, () -> {
                        try {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                        } catch (Exception e) {
                            ServerHelperBridge.instance.logger.warning(ThrowableUtil.stackTraceToString(e));
                        }
                    });
                } catch (Exception e) {
                    ServerHelperBridge.instance.logger.warning(ThrowableUtil.stackTraceToString(e));
                }
                return;
            }

            ServerHelperSender sender = ServerHelperSender.getOrCreateSender(senderName, cl);
            Bukkit.getScheduler().runTask(ServerHelperBridge.instance, () -> {
                try {
                    if (!Bukkit.dispatchCommand(sender, cmd)) {
                        handler.ctx.writeAndFlush(new CmdExecFailedMessage(
                                senderName, message.serverName,
                                "执行错误：未找到指令：" + cmd));
                    }
                } catch (Exception e) {
                    handler.ctx.writeAndFlush(new CmdExecFailedMessage(
                            senderName, message.serverName,
                            "执行错误：指令执行过程中出现了内部错误，详细信息请查看子服控制台。"));
                    ServerHelperBridge.instance.logger.warning(ThrowableUtil.stackTraceToString(e));
                }
            });
        });

        registerMessage(PlayerCmdExecMessage.class, (handler, message) -> {
            ServerHelperBridge.instance.logger.info(message.playerName + ": " + message.cmd);
            Player player = Bukkit.getPlayer(message.playerName);
            if (player == null || !player.isOnline()) {
                handler.ctx.writeAndFlush(new PlayerCmdExecFailedMessage(
                        message.playerName, message.serverName, message.sender,
                        "执行错误：玩家不在线。"));
                return;
            }

            String cmd = message.cmd.replace("\\", "");
            Bukkit.getScheduler().runTask(ServerHelperBridge.instance, () -> {
                try {
                    player.performCommand(cmd);
                } catch (Exception e) {
                    ServerHelperBridge.instance.logger.warning(ThrowableUtil.stackTraceToString(e));
                }
            });
        });

        registerMessage(OnlineGetMessage.class, (handler, message) -> {
            Collection<? extends Player> onlinePlayers = ServerHelperBridge.instance.getServer().getOnlinePlayers();
            if (message.getPlayerList) {
                List<String> onlinePlayerList = new ArrayList<>();
                for (Player onlinePlayer : onlinePlayers) {
                    onlinePlayerList.add(onlinePlayer.getName());
                }
                ctx.writeAndFlush(new OnlinePlayerListMessage(onlinePlayers.size(), onlinePlayerList.toArray(new String[0])));
            } else {
                ctx.writeAndFlush(new OnlinePlayerListMessage(onlinePlayers.size()));
            }
        });

        registerMessage(ModListGetMessage.class, this::processModListGetMessage);

        registerMessage(PreDisconnectMessage.class, (handler, message) -> {
            ServerHelperBridge.instance.logger.warning("即将与中心服务器断开连接，原因：" + message.reason);
            cl.disconnect();
        });

        registerMessage(HeartbeatResponse.class, (handler, message) -> cl.updateHeartbeatTime());
    }

    @Override
    protected void channelActive0() {
        BridgeClientConfig config = cl.getConfig();

        ServerHelperBridge.instance.logger.info("已连接至中心服务器，注册信息中...");
        ctx.writeAndFlush(new ClientTypeMessage(
                ClientType.SUB_SERVER.toString(),
                ServerHelperBridge.PROTOCOL_VERSION,
                config.getAccessToken(),
                config.getServerName()));

        cl.setCtx(ctx);
    }

    @Override
    protected void channelInactive0() {
        cl.setCtx(null);
        cl.setFuture(null);
    }

    private void processModListGetMessage(MainHandler handler, ModListGetMessage message) {
        try {
            Class<?> loader = Class.forName("net.minecraftforge.fml.common.Loader");
            Method instance = loader.getMethod("instance");
            Object loaderInstance = instance.invoke(null);
            Method getActiveModList = loader.getMethod("getActiveModList");
            List<Object> modContainerList = (List<Object>) getActiveModList.invoke(loaderInstance);

            Class<?> modContainer = Class.forName("net.minecraftforge.fml.common.ModContainer");

            Method getModId = modContainer.getMethod("getModId");
            Method getVersion = modContainer.getMethod("getVersion");

            List<ModItem> list = new ArrayList<>();
            for (Object container : modContainerList) {
                ModItem modItem = new ModItem((String) getModId.invoke(container), (String) getVersion.invoke(container));
                list.add(modItem);
            }
            ctx.writeAndFlush(new ModListMessage(list));

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                final ModItem item = list.get(i);
                sb.append(item.getModID()).append("@").append(item.getVersion());
                if (i + 1 < list.size()) {
                    sb.append(", ");
                } else {
                    sb.append("]");
                }
            }
            ServerHelperBridge.instance.logger.info("已向 BC 服务端推送模组列表：" + sb);
        } catch (Exception e) {
            ServerHelperBridge.instance.logger.warning("获取 Mod 列表时出现错误！");
            ServerHelperBridge.instance.logger.warning(ThrowableUtil.stackTraceToString(e));
        }
    }

}
