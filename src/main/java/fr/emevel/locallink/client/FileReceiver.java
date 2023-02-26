package fr.emevel.locallink.client;

import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileReceiver implements Runnable {

    public static final List<FileReceiver> currentlyReceiving = new ArrayList<>();

    private final File destination;
    private final InetAddress address;
    private final int port;
    private final int bufferSize;
    @Getter
    private final long length;
    @Getter
    private long current = 0;
    private Socket socket;

    public FileReceiver(File destination, InetAddress serverAddress, int port, int bufferSize, long length) {
        this.destination = destination;
        this.address = serverAddress;
        this.port = port;
        this.bufferSize = bufferSize;
        this.length = length;
    }

    private void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while closing file receiver");
            e.printStackTrace();
        }
    }

    private void receiveFile() throws IOException {
        System.out.println("Receiving file " + destination.getName() + " from " + port);
        byte[] buffer = new byte[bufferSize];
        int read;
        try (FileOutputStream fout = new FileOutputStream(destination)) {
            while ((read = socket.getInputStream().read(buffer)) != -1) {
                current += read;
                fout.write(buffer, 0, read);
            }
        }
    }

    private void connect() throws IOException {
        socket = new Socket(address, port);
    }

    @Override
    public void run() {
        currentlyReceiving.add(this);
        try {
            connect();
            receiveFile();
            close();
        } catch (IOException e) {
            System.err.println("Error while receiving file " + destination.getName());
            e.printStackTrace();
        } finally {
            currentlyReceiving.remove(this);
        }
    }
}
