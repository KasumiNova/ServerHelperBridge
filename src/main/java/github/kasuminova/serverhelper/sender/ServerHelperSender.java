package github.kasuminova.serverhelper.sender;

import github.kasuminova.serverhelper.network.BridgeClient;

import java.util.HashMap;
import java.util.Map;

public class ServerHelperSender extends AbstractServerHelperSender {
    public static final Map<String, ServerHelperSender> SENDERS = new HashMap<>();
    private final String senderName;
    private BridgeClient cl;

    public ServerHelperSender(String senderName, BridgeClient cl) {
        this.senderName = senderName;
        this.cl = cl;
    }

    public static ServerHelperSender getOrCreateSender(String senderName, BridgeClient cl) {
        return SENDERS.computeIfAbsent(senderName, name -> new ServerHelperSender(name, cl)).setBridgeClient(cl);
    }

    public ServerHelperSender setBridgeClient(BridgeClient cl) {
        this.cl = cl;
        return this;
    }

    @Override
    public void sendMessage(String message) {
        cl.getCmdExecSyncThread().addExecResult(senderName, message);
    }

    @Override
    public void sendMessage(String[] messages) {
        cl.getCmdExecSyncThread().addExecResult(senderName, messages);
    }

    @Override
    public void sendRawMessage(String message) {
        cl.getCmdExecSyncThread().addExecResult(senderName, message);
    }

    @Override
    public String getName() {
        return senderName;
    }
}
