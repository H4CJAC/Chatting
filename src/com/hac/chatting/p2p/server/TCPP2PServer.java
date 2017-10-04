package com.hac.chatting.p2p.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static com.hac.chatting.conf.TCPConf.BUFSIZE;
import static com.hac.chatting.conf.TCPConf.CHARSET;

public class TCPP2PServer implements Runnable{
    private final int PORT=12346;
    private final Set<InetSocketAddress> adds=new HashSet<>(100);//记录在线ip和port
    private SelectionKey preKey=null;
    private final ReentrantLock lock=new ReentrantLock();
    public static void main(String[] args) {
        TCPP2PServer ser=new TCPP2PServer();
        Thread t=new Thread(ser);
        t.start();
    }

    @Override
    public void run() {
        System.out.println("P2PServer on.");
        ServerSocketChannel ssc;
        Selector selector;
        try {
            ssc=ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(PORT));
            selector=Selector.open();
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Iterator<SelectionKey> ks;
        SelectionKey k;
        SocketChannel sc;
        ByteBuffer buffer=ByteBuffer.allocate(BUFSIZE);
        InetSocketAddress isaddr;
        while (true){
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            ks=selector.selectedKeys().iterator();
            while (ks.hasNext()){
                k=ks.next();
                ks.remove();
                if (k.isAcceptable()){
                    try {
                        sc=ssc.accept();
                        sc.configureBlocking(false);
                        k=sc.register(selector,SelectionKey.OP_WRITE);
                        if (preKey==null)preKey=k;
                        else {
                            preKey.attach(sc.getRemoteAddress());
                            sc= (SocketChannel) preKey.channel();
                            k.attach(sc.getRemoteAddress());
                            preKey=null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        preKey.attach(null);
                        continue;
                    }
                }else if(k.isWritable()&&k.attachment()!=null){
                    sc= (SocketChannel) k.channel();
                    isaddr= (InetSocketAddress) k.attachment();
                    buffer.putInt(isaddr.getPort());
                    buffer.put(isaddr.getHostName().getBytes(CHARSET));
                    buffer.flip();
                    try {
                        InetSocketAddress adr= (InetSocketAddress) sc.getRemoteAddress();
                        System.out.println(isaddr.getHostName()+": "+isaddr.getPort()+" -> "+adr.getHostName()+": "+adr.getPort());
                        sc.write(buffer);
                        sc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    buffer.clear();
                    k.cancel();
                }
            }
        }
    }
}
