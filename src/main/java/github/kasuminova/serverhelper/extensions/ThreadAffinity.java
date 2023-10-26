package github.kasuminova.serverhelper.extensions;

import github.kasuminova.serverhelper.ServerHelperBridge;
import net.openhft.affinity.Affinity;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

public class ThreadAffinity {
    private BitSet affinity = Affinity.getAffinity();

    public void loadFromConfig(FileConfiguration config) {
        affinity = validateAndConvertConfig(config.getIntegerList("ThreadAffinity.ServerThread.Affinity"));
    }

    private BitSet validateAndConvertConfig(List<Integer> config) {
        int maxAvailable = Runtime.getRuntime().availableProcessors();
        ServerHelperBridge.instance.logger.info("当前所有可用 CPU 数量：" + maxAvailable);

        BitSet affinity = new BitSet(config.size());
        for (int cpuId : config) {
            if (cpuId < 0 || cpuId >= maxAvailable) {
                ServerHelperBridge.instance.logger.warning(String.format(
                        "服务端主线程配置中的 CPU #%d 不可用，已忽略。", cpuId)
                );
                continue;
            }
            affinity.set(cpuId);
        }

        if (affinity.isEmpty()) {
            ServerHelperBridge.instance.logger.warning("服务端主线程 CPU 配置无效！使用默认配置（使用全部 CPU）...");
            IntStream.range(0, maxAvailable).forEach(affinity::set);
        }

        return affinity;
    }

    public void setThreadAffinity() {
        Affinity.setAffinity(affinity);

        ServerHelperBridge.instance.logger.info(String.format(
                "设置服务端主线程 CPU 分配成功！现在限定 CPU：%s", affinity.toString())
        );
    }
}
