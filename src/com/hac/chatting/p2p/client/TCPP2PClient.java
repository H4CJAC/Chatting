package com.hac.chatting.p2p.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import static com.hac.chatting.conf.TCPConf.BUFSIZE;
import static com.hac.chatting.conf.TCPConf.CHARSET;

public class TCPP2PClient implements Runnable{
//    private final String SERVERHOST="localhost";
    private final String SERVERHOST="192.168.0.104";
    private final int SERVERPORT=12346;
    public static void main(String[] args) {
        TCPP2PClient cli=new TCPP2PClient();
        Thread t=new Thread(cli);
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P2PClient on.");
        SocketChannel sc;
        ByteBuffer buffer=ByteBuffer.allocate(BUFSIZE);
        try{
            sc=SocketChannel.open();
            sc.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            sc.connect(new InetSocketAddress(SERVERHOST,SERVERPORT));
            sc.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        buffer.flip();
        int port=buffer.getInt();
        byte[] bs=new byte[buffer.remaining()];
        buffer.get(bs);
        buffer.clear();
        String hostname=new String(bs,CHARSET);
        System.out.println("hostname: "+hostname+" port: "+port);
        InetSocketAddress maddr;
        try {
            maddr= (InetSocketAddress) sc.getLocalAddress();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            System.out.println(maddr.getHostName()+": "+maddr.getPort());
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        while (!sc.isOpen()){
            try {
                sc=SocketChannel.open();
                sc.setOption(StandardSocketOptions.SO_REUSEADDR,true);
                sc.bind(maddr);
                sc.connect(new InetSocketAddress(hostname,port));//?refuse
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            buffer.put(("Hello i'm "+maddr.getHostName()+": "+maddr.getPort()+".").getBytes(CHARSET));
            buffer.flip();
            sc.write(buffer);
            buffer.clear();
            sc.read(buffer);
            buffer.flip();
            System.out.println(new String(buffer.array(),CHARSET));
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
