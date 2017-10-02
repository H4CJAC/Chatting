package com.hac.chatting.p2p.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

public class P2PClient {
    private final static String SERVERHOST="localhost";
    private final static int SERVERPORT=22345;
    private final static int BUFSIZE=52;
    private final static Set<InetSocketAddress> adds=new HashSet<>(100);
    public static void main(String[] args) {
        try(
                DatagramChannel dc=DatagramChannel.open();
        ) {
//            dc.receive()
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
