package fr.emevel.locallink;

import fr.emevel.locallink.network.InetAddressUtils;
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

        client.sendPacket(new PacketHandShake("LocalLink Client", "1.0"));

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        client.stop();
    }
}