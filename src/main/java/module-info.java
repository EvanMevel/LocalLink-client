module locallink.client {
    requires locallink.network;
    requires static lombok;
    requires javax.jmdns;
    requires org.apache.commons.collections4;

    exports fr.emevel.locallink.client;
}