package fr.emevel.locallink.client;

import lombok.Data;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class LocalLinkClientData implements Serializable {

    private UUID uuid = UUID.randomUUID();
    private String name = InetAddress.getLocalHost().getHostName();
    private Map<UUID, File> folders = new HashMap<>();

    public LocalLinkClientData() throws UnknownHostException {
    }

}
