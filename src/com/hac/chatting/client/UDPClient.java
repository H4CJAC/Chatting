package com.hac.chatting.client;

import com.hac.chatting.DTO.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hac.chatting.conf.UDPConf.BUFSIZE;
import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPClient implements Runnable {

    private final String SERHOST="localhost";
    private final int SERPORT=23456;
    private final LinkedBlockingQueue<Message> writeQueue=new LinkedBlockingQueue<>();
    private final Hashtable<Integer,Message> ackTable=new Hashtable<>(100),ackedTable=new Hashtable<>(100);
    private long myId;

    public UDPClient(long myId) {
        this.myId = myId;
    }

    public static void main(String[] args) {
        UDPClient cli=new UDPClient(13);
        Thread t=new Thread(cli);
        t.start();
    }

    @Override
    public void run() {
        InetSocketAddress seraddr=new InetSocketAddress(SERHOST,SERPORT);
        System.out.println("UDPCliWriteThread on.");
        try (
                DatagramSocket ds=new DatagramSocket(0);
        ){
            UDPCliInputThread udpCliInputThread=new UDPCliInputThread(seraddr,writeQueue,myId);
            UDPCliReadThread udpCliReadThread=new UDPCliReadThread(writeQueue,ackTable,ackedTable,ds,seraddr);
            UDPCliCheckThread udpCliCheckThread =new UDPCliCheckThread(ackTable,ackedTable, writeQueue);
            Thread ucit=new Thread(udpCliInputThread),ucrt=new Thread(udpCliReadThread),ucct=new Thread(udpCliCheckThread);
            ucit.start();ucrt.start();ucct.start();
            byte[] bs=new byte[BUFSIZE];
            DatagramPacket dp=new DatagramPacket(bs,bs.length);
            int bslen;
            Message msg;
            while (true){
                msg=writeQueue.take();
                bslen=msg.getBytes(bs,CHARSET);
                dp.setLength(bslen);
                dp.setSocketAddress(msg.getAddr());
                ds.send(dp);
                if (msg.getCode()!=3){//除了ack，都放入应读表ackTable
                    msg.setLastms(System.currentTimeMillis());
                    ackTable.put(msg.getHcode(),msg);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
