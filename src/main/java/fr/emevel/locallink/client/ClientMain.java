package fr.emevel.locallink.client;

import fr.emevel.locallink.network.InetAddressUtils;
import fr.emevel.locallink.network.Signatures;
import fr.emevel.locallink.network.packets.PacketHandShake;
import fr.emevel.locallink.network.serial.PacketParsingException;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    public static LocalLinkClientServer server;

    private static void save(LocalLinkClientData data) throws IOException {
        File file = new File("client.dat");
        if (!file.exists()) {
            file.createNewFile();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(data);
        }
    }

    private static LocalLinkClientData loadData() throws IOException {
        File file = new File("client.dat");
        if (!file.exists()) {
            LocalLinkClientData data = new LocalLinkClientData();
            save(data);
            return data;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            return (LocalLinkClientData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, PacketParsingException {

        LocalLinkClientData data = loadData();

        Socket socket = new Socket(InetAddressUtils.getLocalHostLANAddress(), 4242);

        server = new LocalLinkClientServer(data, socket);

        server.start();

        server.sendPacket(new PacketHandShake(data.getName(), data.getUuid(), Signatures.VERSION));

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        server.stop();
    }
}