package fr.emevel.locallink.client;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.Packet;
import fr.emevel.locallink.network.PacketConsumerList;
import fr.emevel.locallink.network.packets.PacketHandShake;

import java.io.IOException;
import java.net.Socket;

public class LocalLinkClientServer extends LinkSocket {

    private final PacketConsumerList packetConsumerList = new PacketConsumerList();

    public LocalLinkClientServer(Socket socket) throws IOException {
        super(socket);
        packetConsumerList.addConsumer(PacketHandShake.class, this::handshake);
    }

    private void handshake(PacketHandShake packet) {
        System.out.println("Received handshake packet " + packet);
    }

    @Override
    protected void onPacketReceived(Packet packet) throws IOException {
        System.out.println("Received packet " + packet);
        packetConsumerList.consumePacket(packet);
    }
}
