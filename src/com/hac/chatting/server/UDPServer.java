package com.hac.chatting.server;

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

public class UDPServer implements Runnable {

    private final int PORT=23456;
    private final LinkedBlockingQueue<Message> writeQueue=new LinkedBlockingQueue<>();
    private final Hashtable<String,Message> ackTable=new Hashtable<>(100),ackedTable=new Hashtable<>(100);
    private final Hashtable<String,Long> user2UidTable;
    private final Hashtable<Long,InetSocketAddress> onlineTable;

    public UDPServer(Hashtable<String, Long> user2UidTable, Hashtable<Long, InetSocketAddress> onlineTable) {
        this.user2UidTable = user2UidTable;
        this.onlineTable = onlineTable;
    }

    @Override
    public void run() {
        System.out.println("UDPSerWriteThread on.");
        try (
                DatagramSocket ds=new DatagramSocket(PORT);
                ){
            UDPSerReadThread udpSerReadThread=new UDPSerReadThread(writeQueue,ackTable,ackedTable,onlineTable,ds);
            UDPSerCheckThread udpSerCheckThread =new UDPSerCheckThread(ackTable,ackedTable, onlineTable, writeQueue, user2UidTable);
            Thread usrt=new Thread(udpSerReadThread);
            Thread usct=new Thread(udpSerCheckThread);
            usrt.start();usct.start();
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
