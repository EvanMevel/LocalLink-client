package fr.emevel.locallink.client;

import fr.emevel.locallink.network.Signatures;
import fr.emevel.locallink.network.jmdns.LinkJmdns;
import fr.emevel.locallink.network.jmdns.client.JmDNSClient;
import fr.emevel.locallink.network.packets.PacketHandShake;

import javax.jmdns.ServiceInfo;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.BiConsumer;

public class LocalLinkClient implements BiConsumer<InetSocketAddress, ServiceInfo> {

    private final JmDNSClient client;
    private final File baseFolder;
    private final LocalLinkClientData data;
    private final Runnable dataSaver;
    LocalLinkClientServer server = null;

    public LocalLinkClient(LocalLinkClientData data, File baseFolder, Runnable dataSaver) {
        this.data = data;
        this.baseFolder = baseFolder;
        this.dataSaver = dataSaver;
        client = new JmDNSClient(this, data.getUuid());
    }

    public void start() throws IOException {
        client.registerListener();
    }

    public void stop() throws IOException {
        client.stop();
        if (server != null) {
            server.stop();
        }
    }

    protected void connected() throws IOException {
        server.start();

        server.sendPacket(new PacketHandShake(data.getName(), data.getUuid(), Signatures.VERSION));
    }

    @Override
    public void accept(InetSocketAddress inetSocketAddress, ServiceInfo serviceInfo) {
        System.out.println("Service found: " + serviceInfo);
        if (!LinkJmdns.SERVICE_TYPE.equals(serviceInfo.getType())) {
            return;
        }
        System.out.println("Found a LocalLink service at " + inetSocketAddress);
        if (server != null) {
            System.out.println("Already connected to a LocalLink service");
            return;
        }
        try {
            Socket socket = new Socket(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
            server = new LocalLinkClientServer(data, socket, baseFolder, dataSaver);
            connected();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
