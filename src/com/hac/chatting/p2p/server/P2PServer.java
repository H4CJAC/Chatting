package com.hac.chatting.p2p.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class P2PServer {
    private final static int PORT=22345;
    private final static int BUFSIZE=52;
    private final static Charset CHARSET=Charset.forName("utf8");
    private final static Set<InetSocketAddress> adds=new HashSet<>(100);//记录在线ip和port
    public static void main(String[] args) {
        try (
                DatagramChannel dc=DatagramChannel.open();
        ){
            dc.bind(new InetSocketAddress(PORT));
            ByteBuffer buffer=ByteBuffer.allocate(BUFSIZE);
            while (true){
                InetSocketAddress cli= (InetSocketAddress) dc.receive(buffer);
                for(InetSocketAddress add:adds){
                    buffer.clear();
                    buffer.put((add.getHostName()+" "+add.getPort()+";").getBytes(CHARSET));
                    dc.send(buffer,cli);
                    buffer.clear();
                    buffer.put((cli.getHostName()+" "+cli.getPort()+";").getBytes(CHARSET));
                    buffer.flip();
                    dc.send(buffer,add);
                }
                adds.add(cli);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
