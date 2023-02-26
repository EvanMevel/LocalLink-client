package fr.emevel.locallink.client;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileReceiverExecutor {

    private final ExecutorService executor;
    private final InetAddress address;
    private final int bufferSize;

    public FileReceiverExecutor(InetAddress address, int bufferSize) {
        this.address = address;
        this.bufferSize = bufferSize;
        this.executor = Executors.newCachedThreadPool();
    }

    public void stop() throws InterruptedException {
        executor.shutdown();
        System.out.println("Waiting for File Receivers to stop");
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("File Receivers didn't stop in time, forcing shutdown");
            executor.shutdownNow();
        }
    }

    public void execute(File destination, int port, long length) {
        executor.execute(new FileReceiver(destination, address, port, bufferSize, length));
    }
}
