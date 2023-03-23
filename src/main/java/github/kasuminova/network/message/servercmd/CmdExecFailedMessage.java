package github.kasuminova.network.message.servercmd;

import java.io.Serializable;

public class CmdExecFailedMessage implements Serializable {
    String cause;

    public CmdExecFailedMessage(String cause) {
        this.cause = cause;
    }
}
