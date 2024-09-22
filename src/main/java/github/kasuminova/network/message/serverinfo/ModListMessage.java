package github.kasuminova.network.message.serverinfo;

import java.io.Serializable;
import java.util.List;

public class ModListMessage implements Serializable {

    private List<ModItem> modList;

    public ModListMessage(final List<ModItem> modList) {
        this.modList = modList;
    }

    public List<ModItem> getModList() {
        return modList;
    }

    public void setModList(final List<ModItem> modList) {
        this.modList = modList;
    }
}
