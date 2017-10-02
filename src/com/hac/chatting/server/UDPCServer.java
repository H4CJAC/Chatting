package com.hac.chatting.server;

import com.hac.chatting.DTO.Msg;
import com.hac.chatting.conf.UDPConf;
import com.hac.chatting.utils.CastUtil;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.hac.chatting.conf.UDPConf.BUFSIZE;
import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPCServer implements Runnable{

    private final static int PORT=12346;
    private volatile boolean stopped=false;
    private final Map<Long,InetSocketAddress> onlineIds=new HashMap<>(100);

    public static void main(String[] args) {
        UDPCServer server=new UDPCServer();
        Thread t=new Thread(server);
        t.run();
        Scanner s=new Scanner(System.in);
        while(!"quit".equals(s.nextLine()));
        server.halt();
    }

    public void halt(){
        stopped=true;
    }

    private void process(DatagramChannel dc,ByteBuffer buffer,InetSocketAddress saddr) throws IOException {
        buffer.flip();
        if (buffer.limit()<9){//防止opcode和sid解析错误
            buffer.clear();
            Msg.setBuffer(buffer, (byte) -1,0l,"please login".getBytes(CHARSET));
            buffer.flip();
            dc.send(buffer,saddr);
            return;
        }
        byte opcode=buffer.get();
        Long sid=buffer.getLong();
        switch (opcode){
            case 0://上线
                buffer.clear();
                Msg.setBuffer(buffer, (byte) 0,0l,"ok.".getBytes(CHARSET));
                buffer.flip();
                dc.send(buffer,saddr);
                onlineIds.put(sid,saddr);
                System.out.println(sid+" "+saddr.getHostName()+": "+saddr.getPort()+" online.");
                break;
            case 1://聊天
                if(buffer.remaining()<8){//防止目标id解析错误
                    buffer.clear();
                    Msg.setBuffer(buffer, (byte) -1,0l,"send fail.".getBytes(CHARSET));
                    buffer.flip();
                    dc.send(buffer,saddr);
                    System.out.println("sf");
                    break;
                }
                Long did=buffer.getLong();
                InetSocketAddress daddr=onlineIds.get(did);
                if (daddr==null){//目标不在线
                    buffer.clear();
                    Msg.setBuffer(buffer, (byte) -1,0l,"target not online.".getBytes(CHARSET));
                    buffer.flip();
                    dc.send(buffer,saddr);
                    System.out.println("tno");
                    break;
                }
                byte[] opbs=new byte[buffer.remaining()];
                buffer.get(opbs);
                buffer.clear();
                Msg.setBuffer(buffer, (byte) 0,sid,opbs);
                buffer.flip();
                dc.send(buffer,daddr);
                System.out.println("opcode: "+opcode+" sid: "+sid+" did: "+did+" msg: "+new String(opbs,CHARSET));
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        try(
                DatagramChannel dc=DatagramChannel.open();
        ){
            dc.bind(new InetSocketAddress(PORT));
            ByteBuffer buffer=ByteBuffer.allocate(BUFSIZE);
            while(!stopped){
                InetSocketAddress saddr= (InetSocketAddress) dc.receive(buffer);
                process(dc,buffer,saddr);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
