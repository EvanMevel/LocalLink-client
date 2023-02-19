package fr.emevel.locallink;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.Packet;

import java.io.IOException;
import java.net.Socket;

public class LocalLinkClientServer extends LinkSocket {

    public LocalLinkClientServer(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    protected void onPacketReceived(Packet packet) throws IOException {
        System.out.println("Received packet " + packet);
    }
}
