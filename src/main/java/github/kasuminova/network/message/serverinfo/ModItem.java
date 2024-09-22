package github.kasuminova.network.message.serverinfo;

import java.io.Serializable;

public class ModItem implements Serializable {

    private String modID;
    private String version;

    public ModItem(final String modID, final String version) {
        this.modID = modID;
        this.version = version;
    }

    public String getModID() {
        return this.modID;
    }

    public String getVersion() {
        return this.version;
    }

    public void setModID(String modID) {
        this.modID = modID;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
