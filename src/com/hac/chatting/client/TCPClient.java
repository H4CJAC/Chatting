package com.hac.chatting.client;

import com.hac.chatting.DTO.Message;
import com.hac.chatting.DTO.TCPMsg;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static com.hac.chatting.conf.TCPConf.BUFSIZE;
import static com.hac.chatting.conf.TCPConf.CHARSET;

public class TCPClient implements Runnable {
    private final String SERHOST="localhost";
    private final int SERPORT=12345;

    public static void main(String[] args) {
        TCPClient cli=new TCPClient();
        Thread t=new Thread(cli);
        t.start();
    }

    @Override
    public void run() {
        System.out.println("TCPClient on.");
        try (
                SocketChannel socketChannel=SocketChannel.open();
                ){
            ByteBuffer buffer=ByteBuffer.allocate(BUFSIZE);
            String username;
            Scanner scanner=new Scanner(System.in);
            while((username=scanner.nextLine()).length()>25) System.out.println("Username's size has to be no more than 25.");
            TCPMsg msg=new TCPMsg((byte)0,(byte)0,0l,username);
            System.out.println("code: "+msg.getCode()+" status: "+msg.getStatus()+" uid: "+msg.getUid()+" Username: "+msg.getUsername());
            msg.getBuffer(buffer,CHARSET);
            buffer.flip();
            socketChannel.connect(new InetSocketAddress(SERHOST,SERPORT));
            socketChannel.write(buffer);
            buffer.clear();
            socketChannel.read(buffer);
            buffer.flip();
            msg=new TCPMsg(buffer,CHARSET);
            buffer.clear();
            System.out.println("code: "+msg.getCode()+" status: "+msg.getStatus()+" uid: "+msg.getUid()+" Username: "+msg.getUsername());
            if (msg.getStatus()==-1)return;
            UDPClient cli=new UDPClient(msg.getUid());
            Thread t=new Thread(cli);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
