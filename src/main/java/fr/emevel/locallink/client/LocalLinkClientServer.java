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

    private final File baseFolder;
    private final LocalLinkClientData data;
    private final Runnable dataSaver;

    public LocalLinkClientServer(LocalLinkClientData data, Socket socket, File baseFolder, Runnable dataSaver) throws IOException {
        super(socket);
        this.data = data;
        this.baseFolder = baseFolder;
        this.dataSaver = dataSaver;
        packetConsumerList.addConsumer(PacketHandShake.class, this::handshake);
        packetConsumerList.addConsumer(PacketAskFolders.class, this::askedFolders);
        packetConsumerList.addConsumer(PacketAskFiles.class, this::askedFiles);
        packetConsumerList.addConsumer(PacketSendFile.class, this::sendingFile);
        packetConsumerList.addConsumer(PacketDeleteFiles.class, this::deleteFiles);
        packetConsumerList.addConsumer(PacketCreateLink.class, this::createLink);

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
                PacketFolderList.builder().setDeep(false).addFolder(baseFolder).build()
        );
    }

    private void askedFiles(PacketAskFiles packet) {
        System.out.println("Received ask file packet " + packet);

        File folder = data.getFolders().get(packet.getFolder());
        if (folder == null) {
            System.out.println("Folder " + packet.getFolder() + " not found");
            return;
        }
        try {
            List<PacketFileList> packets = PacketFileList.builder().processFolder(packet.getFolder(), folder).build();
            safeSendPacket(packets);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendingFile(PacketSendFile packet) {
        System.out.println("Received file packet " + packet);
        File folder = data.getFolders().get(packet.getFolder());
        if (folder == null) {
            System.out.println("Folder " + packet.getFolder() + " not found");
            return;
        }
        File file = new File(folder, packet.getName());
        fileReceiverExecutor.execute(file, packet.getPort(), packet.getLength());
    }

    private void deleteFiles(PacketDeleteFiles packet) {
        System.out.println("Received delete files packet " + packet);
        File folder = data.getFolders().get(packet.getFolder());
        if (folder == null) {
            System.out.println("Folder " + packet.getFolder() + " not found");
            return;
        }
        for (String file : packet.getFiles()) {
            System.out.println("Deleting file " + new File(folder, file).getAbsoluteFile());
            new File(folder, file).delete();
        }
    }

    private void createLink(PacketCreateLink packet) {
        System.out.println("Received link folder packet " + packet);
        File folder = new File(baseFolder, packet.getFolderName());
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.err.println("Failed to create folder " + folder.getAbsolutePath());
                return;
            }
        }
        data.getFolders().put(packet.getFolderUuid(), folder);
        dataSaver.run();
        safeSendPacket(new PacketLinkCreated(packet.getFolderUuid()));
    }

    @Override
    protected void onPacketReceived(Packet packet) throws IOException {
        System.out.println("Received packet " + packet);
        packetConsumerList.consumePacket(packet);
    }
}
