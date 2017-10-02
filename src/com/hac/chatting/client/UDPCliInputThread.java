package com.hac.chatting.client;

import com.hac.chatting.DTO.Message;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPCliInputThread implements Runnable {
    private final InetSocketAddress seraddr;
    private final LinkedBlockingQueue<Message> writeQueue;
    private long myId;

    public UDPCliInputThread(InetSocketAddress seraddr, LinkedBlockingQueue<Message> writeQueue,long myId) {
        this.seraddr = seraddr;
        this.writeQueue = writeQueue;
        this.myId=myId;
    }

    @Override
    public void run() {
        System.out.println("UDPCliInputThread on.");
        Scanner scanner=new Scanner(System.in);
        String input;
        long did;
        Message msg;
        msg=new Message((byte) 0,(byte) 0,0,myId,0l,"",seraddr);
        msg.setHcode(msg.hashCode());
        try {
            writeQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            while (true){
                System.out.println("Please input DID:");
                input=scanner.nextLine();
                try {
                    did=Long.parseLong(input);
                }catch (NumberFormatException nfe){
                    System.out.println("DID format not correct.");
                    continue;
                }
                System.out.println("Please input message:");
                input=scanner.nextLine();
                if (input.length()>450){
                    System.out.println("Message's size has to be no more than 450.");
                    continue;
                }else {
                    msg=new Message((byte) 2,(byte) 0,0,myId,did,input,seraddr);
                    msg.setHcode(msg.hashCode());
                    writeQueue.put(msg);
                }
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
