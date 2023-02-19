package fr.emevel.locallink.client;

import fr.emevel.locallink.network.InetAddressUtils;
import fr.emevel.locallink.network.Signatures;
import fr.emevel.locallink.network.packets.PacketHandShake;
import fr.emevel.locallink.network.serial.PacketParsingException;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException, PacketParsingException {
        Socket socket = new Socket(InetAddressUtils.getLocalHostLANAddress(), 4242);

        LocalLinkClientServer client = new LocalLinkClientServer(socket);

        client.start();

        client.sendPacket(new PacketHandShake("LocalLink Client", Signatures.VERSION));

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        client.stop();
    }
}