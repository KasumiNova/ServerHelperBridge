package github.kasuminova.network.message.servercmd;

import java.io.Serializable;

public class CmdExecMessage implements Serializable {
    public String sender;
    public String serverName;
    public String cmd;

    public CmdExecMessage(String sender, String serverName, String cmd) {
        this.sender = sender;
        this.serverName = serverName;
        this.cmd = cmd;
    }
}
