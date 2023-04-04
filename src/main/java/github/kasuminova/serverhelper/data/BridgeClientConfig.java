package github.kasuminova.serverhelper.data;

import org.bukkit.configuration.file.FileConfiguration;

public class BridgeClientConfig {
    private String ip = "127.0.0.1";
    private int port = 20000;
    private String serverName = "SERVER";
    private String accessToken = "123abc";

    public void loadFromConfig(FileConfiguration config) {
        ip = config.getString("BridgeClient.IP", "127.0.0.1");
        port = config.getInt("BridgeClient.Port", 20000);
        serverName = config.getString("BridgeClient.ServerName", "SERVER");
        accessToken = config.getString("BridgeClient.AccessToken", "123abc");
    }

    public String getIp() {
        return ip;
    }

    public BridgeClientConfig setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public BridgeClientConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public BridgeClientConfig setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public BridgeClientConfig setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }
}
