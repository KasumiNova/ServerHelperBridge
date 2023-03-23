package github.kasuminova.kasuminovabot.module.serverhelper;

import github.kasuminova.kasuminovabot.KasumiNovaBot2;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionDaemonThread implements Runnable {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    private final ServerHelperCL cl;
    private Thread thread;

    public ConnectionDaemonThread(ServerHelperCL cl) {
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
        KasumiNovaBot2.INSTANCE.logger.info("连接守护线程已启动.");

        while (!Thread.currentThread().isInterrupted()) {
            if (cl.future == null) {
                KasumiNovaBot2.INSTANCE.logger.warning("已失去对插件服务器的连接！正在尝试重连...");

                boolean isConnected = false;
                int retryCount = 0;
                while (retryCount < 5) {
                    try {
                        cl.connect();
                        isConnected = true;
                        break;
                    } catch (Exception e) {
                        KasumiNovaBot2.INSTANCE.logger.warning("重连错误: " + e);
                        KasumiNovaBot2.INSTANCE.logger.info("等待 5 秒后重试...");

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            KasumiNovaBot2.INSTANCE.logger.warning("连接守护线程被中断.");
                        }
                    }
                    retryCount++;
                }

                if (!isConnected) {
                    try {
                        KasumiNovaBot2.INSTANCE.logger.warning("重试已超过 5 次，等待 30 秒后重新连接.");
                        Thread.sleep(30000);
                        continue;
                    } catch (InterruptedException e) {
                        KasumiNovaBot2.INSTANCE.logger.warning("连接守护线程被中断.");
                        break;
                    }
                }
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                KasumiNovaBot2.INSTANCE.logger.warning("连接守护线程被中断.");
                break;
            }
        }
    }
}
