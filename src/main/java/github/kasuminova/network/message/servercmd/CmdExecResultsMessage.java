package github.kasuminova.network.message.servercmd;

import java.io.Serializable;

public class CmdExecResultMessage implements Serializable {
    public String serverName;
    public String sender;
    public String[] results;
    public CmdExecResultMessage(String serverName, String sender, String[] results) {
        this.serverName = serverName;
        this.sender = sender;
        this.results = results;
    }
}
