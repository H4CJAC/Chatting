package com.hac.chatting.server;

import com.hac.chatting.DTO.TCPMsg;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;

import static com.hac.chatting.conf.TCPConf.BUFSIZE;
import static com.hac.chatting.conf.TCPConf.CHARSET;

public class TCPServer implements Runnable {
    private final Hashtable<String,Long> user2UidTable=new Hashtable<>(100);
    private final Hashtable<Long,InetSocketAddress> onlineTable=new Hashtable<>(100);
    private final int PORT=12345;

    public static void main(String[] args) {
        TCPServer ser=new TCPServer();
        Thread t=new Thread(ser);
        t.start();
    }

    private TCPMsg processRead(TCPMsg msg){
        switch (msg.getCode()){
            case 0://登陆
                synchronized (user2UidTable){
                    if(msg.getUsername().length()>25){
                        msg.setStatus((byte) -2);//名字过长
                        msg.setUsername("");
                    }else if (user2UidTable.containsKey(msg.getUsername())&&onlineTable.containsKey(user2UidTable.get(msg.getUsername()))) {
                        msg.setStatus((byte) -1);//用户名已存在
                        msg.setUsername("");
                    }else {
                        long uid=msg.hashCode()|(System.currentTimeMillis()<<32);
                        user2UidTable.put(msg.getUsername(),uid);
                        msg.setUid(uid);
                        msg.setUsername("");
                    }
                }
                break;
            default:break;
        }
        return msg;
    }

    @Override
    public void run() {
        UDPServer udpser=new UDPServer(user2UidTable, onlineTable);
        Thread t=new Thread(udpser);
        t.start();
        System.out.println("TCPServer on.");
        ServerSocketChannel ssc=null;
        Selector selector=null;
        try {
            ssc=ServerSocketChannel.open();
            selector=Selector.open();
            ssc.bind(new InetSocketAddress(PORT));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ssc!=null&&selector!=null){
            Iterator<SelectionKey> ks=null;
            SelectionKey k;
            SocketChannel sc=null;
            TCPMsg msg;
            ByteBuffer buffer=ByteBuffer.allocate(BUFSIZE);
            while (true){
                try {
                    selector.select();
                    ks=selector.selectedKeys().iterator();
                    while (ks.hasNext()){
                        k=ks.next();
                        ks.remove();
                        if (k.isAcceptable()){
                            sc=ssc.accept();
                            sc.configureBlocking(false);
                            sc.register(selector,SelectionKey.OP_READ);
                        }else {
                            if (k.isReadable()){
                                sc= (SocketChannel) k.channel();
                                while (buffer.hasRemaining()&&sc.read(buffer)>0);
                                buffer.flip();
                                msg=new TCPMsg(buffer,CHARSET);
                                processRead(msg);
                                k.attach(msg);
                                k.interestOps(SelectionKey.OP_WRITE);
                                buffer.clear();
                            }
                            if (k.isWritable()&&k.attachment()!=null){
                                sc= (SocketChannel) k.channel();
                                msg= (TCPMsg) k.attachment();
                                msg.getBuffer(buffer,CHARSET);
                                buffer.flip();
                                sc.write(buffer);
                                buffer.clear();
                                sc.close();
                                k.cancel();
                            }
                        }
                    }
                }catch (IOException e){
                    if(sc!=null) {
                        try {
                            sc.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    e.printStackTrace();
                }
            }
        }
    }
}
