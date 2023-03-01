package fr.emevel.locallink.client;

import fr.emevel.locallink.network.serial.PacketParsingException;

import java.io.*;
import java.util.Scanner;

public class ClientMain {

    public static void saveDataToFile(LocalLinkClientData data, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(data);
        }
    }

    public static LocalLinkClientData loadDataFromFile(File file) throws IOException {
        if (!file.exists()) {
            LocalLinkClientData data = new LocalLinkClientData();
            saveDataToFile(data, file);
            return data;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            return (LocalLinkClientData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, PacketParsingException {
        LocalLinkClientData data = loadDataFromFile(new File("client.dat"));

        Runnable saver = () -> {
            try {
                saveDataToFile(data, new File("client.dat"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        LocalLinkClient client = new LocalLinkClient(data, new File("client"), saver);

        client.start();

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        client.stop();
    }
}