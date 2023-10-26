package github.kasuminova.serverhelper.network;

import github.kasuminova.network.message.protocol.HeartbeatMessage;
import github.kasuminova.serverhelper.ServerHelperBridge;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class ConnectionDaemonThread implements Runnable {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    private final BridgeClient cl;
    private Thread thread;

    public ConnectionDaemonThread(BridgeClient cl) {
        this.cl = cl;
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.setName("ConnectionDaemonThread-" + THREAD_COUNT.getAndIncrement());
            thread.start();
        }
    }

    public void interrupt() {
        if (!thread.isInterrupted()) thread.interrupt();
    }

    @Override
    public void run() {
        cl.updateHeartbeatTime();

        ServerHelperBridge.instance.logger.info("连接守护线程已启动.");

        logic:
        while (!Thread.currentThread().isInterrupted()) {
            LockSupport.parkNanos(1000L * 1000 * 1000);

            ChannelFuture future = cl.getFuture();
            if (future != null) {
                if (cl.getLastHeartbeat() + 30000 <= System.currentTimeMillis()) {
                    ServerHelperBridge.instance.logger.warning("中心服务器响应超时。");
                    cl.disconnect();
                    continue;
                }

                future.channel().writeAndFlush(new HeartbeatMessage());
                continue;
            }

            ServerHelperBridge.instance.logger.warning("已失去对中心服务器的连接！正在尝试重连...");
            boolean isConnected = false;
            int retryCount = 0;
            do {
                cl.updateHeartbeatTime();
                try {
                    cl.connect();
                    isConnected = true;
                    break;
                } catch (Exception e) {
                    ServerHelperBridge.instance.logger.warning("重连错误: " + e);
                    ServerHelperBridge.instance.logger.info("等待 5 秒后重试...");

                    LockSupport.parkNanos(5L * 1000 * 1000 * 1000);
                    if (Thread.currentThread().isInterrupted()) {
                        break logic;
                    }
                }
                retryCount++;
            } while (retryCount <= 5);

            if (!isConnected) {
                ServerHelperBridge.instance.logger.warning("重试已超过 5 次，等待 30 秒后重新连接.");
                LockSupport.parkNanos(30L * 1000 * 1000 * 1000);
            }
        }

        ServerHelperBridge.instance.logger.info("连接守护线程已终止.");
        THREAD_COUNT.getAndDecrement();
    }
}
