package fr.emevel.locallink.client;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.PacketConsumerList;
import fr.emevel.locallink.network.packets.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class LocalLinkClientServer extends LinkSocket {

    private final PacketConsumerList packetConsumerList = new PacketConsumerList();

    private final FileReceiverExecutor fileReceiverExecutor;

    private final File baseFolder = new File("client");

    public LocalLinkClientServer(Socket socket) throws IOException {
        super(socket);
        packetConsumerList.addConsumer(PacketHandShake.class, this::handshake);
        packetConsumerList.addConsumer(PacketAskFolders.class, this::askedFolders);
        packetConsumerList.addConsumer(PacketAskFiles.class, this::askedFiles);
        packetConsumerList.addConsumer(PacketSendFile.class, this::sendingFile);
        packetConsumerList.addConsumer(PacketDeleteFiles.class, this::deleteFiles);

        fileReceiverExecutor = new FileReceiverExecutor(socket.getInetAddress(), 1024);
    }

    @Override
    public void stop() throws IOException {
        try {
            fileReceiverExecutor.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            super.stop();
        }
    }

    private void handshake(PacketHandShake packet) {
        System.out.println("Received handshake packet " + packet);
    }

    private void askedFolders(PacketAskFolders packet) {
        safeSendPacket(
                PacketFolderList.builder().addFolder(baseFolder).build()
        );
    }

    private void askedFiles(PacketAskFiles packet) {
        System.out.println("Received folder list packet " + packet);
        File folder = new File(baseFolder, packet.getFolder());
        try {
            List<PacketFileList> packets = PacketFileList.builder().processFolder(folder).build();
            safeSendPacket(packets);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendingFile(PacketSendFile packet) {
        System.out.println("Received file packet " + packet);
        File file = new File(baseFolder, packet.getFolder() + File.separator + packet.getName());
        fileReceiverExecutor.execute(file, packet.getPort(), packet.getLength());
    }

    private void deleteFiles(PacketDeleteFiles packet) {
        System.out.println("Received delete files packet " + packet);
        File folder = new File(baseFolder, packet.getFolder());
        for (String file : packet.getFiles()) {
            System.out.println("Deleting file " + new File(folder, file).getAbsoluteFile());
            new File(folder, file).delete();
        }
    }

    @Override
    protected void onPacketReceived(Packet packet) throws IOException {
        System.out.println("Received packet " + packet);
        packetConsumerList.consumePacket(packet);
    }
}
