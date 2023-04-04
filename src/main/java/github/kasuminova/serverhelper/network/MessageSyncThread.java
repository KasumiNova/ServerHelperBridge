package github.kasuminova.serverhelper.network;

import github.kasuminova.network.message.servercmd.CmdExecResultsMessage;
import github.kasuminova.serverhelper.ServerHelperBridge;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class MessageSyncThread implements Runnable {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    private final Map<String, LinkedBlockingQueue<String[]>> waitForSend = new HashMap<>();
    private final BridgeClient cl;
    private Thread thread;

    public MessageSyncThread(BridgeClient cl) {
        this.cl = cl;
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
            thread.setName("CmdExecSyncThread-" + THREAD_COUNT.getAndIncrement());
            thread.start();
        }
    }

    public void interrupt() {
        if (!thread.isInterrupted()) thread.interrupt();
    }

    public void addExecResult(String sender, String... message) {
        waitForSend.computeIfAbsent(sender, queue -> new LinkedBlockingQueue<>()).offer(message);
    }

    @Override
    public void run() {
        ServerHelperBridge.instance.logger.info("消息同步线程已启动.");

        while (!Thread.currentThread().isInterrupted()) {
            if (waitForSend.isEmpty()) {
                LockSupport.parkNanos(1000L * 1000 * 1000);
                continue;
            }

            waitForSend.forEach((sender, queue) -> {
                List<String> results = new ArrayList<>();
                String[] execResult;
                while ((execResult = queue.poll()) != null) {
                    results.addAll(Arrays.asList(execResult));
                }
                if (!results.isEmpty()) {
                    cl.sendMessageToServer(new CmdExecResultsMessage(cl.getConfig().getServerName(), sender, results.toArray(new String[0])));
                }
            });
            waitForSend.clear();
        }

        ServerHelperBridge.instance.logger.info("消息同步线程已停止.");
        THREAD_COUNT.getAndDecrement();
    }

}
