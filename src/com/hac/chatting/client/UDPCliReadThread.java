package com.hac.chatting.client;

import com.hac.chatting.DTO.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hac.chatting.conf.UDPConf.BUFSIZE;
import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPCliReadThread implements Runnable {
    private final LinkedBlockingQueue<Message> writeQueue;
    private final Hashtable<String,Message> ackTable,ackedTable;
    private final DatagramSocket ds;
    private final InetSocketAddress seraddr;

    public UDPCliReadThread(LinkedBlockingQueue<Message> writeQueue,Hashtable<String, Message> ackTable, Hashtable<String,Message> ackedTable, DatagramSocket ds
            ,InetSocketAddress seraddr) {
        this.writeQueue=writeQueue;
        this.ackTable = ackTable;
        this.ackedTable=ackedTable;
        this.ds = ds;
        this.seraddr=seraddr;
    }

    private void processMsg(Message msg) throws InterruptedException {
        System.out.println("status: "+msg.getStatus()+" "+msg.getSid()+" -> "+msg.getDid()+": "+msg.getMsg());
        //ack包
        msg.setCode((byte) 3);
        msg.setMsg("");
        writeQueue.put(msg);
    }

    private void processOnlineMsg(Message msg) throws InterruptedException {
        System.out.println("Online message. status: "+msg.getStatus()+" "+msg.getSid()+" -> "+msg.getDid()+": "+msg.getMsg());
        //ack包
        msg.setCode((byte) 3);
        msg.setMsg("");
        writeQueue.put(msg);
    }

    private void processHeart(Message msg) throws InterruptedException {
//        System.out.println("Heartbreak. status: "+msg.getStatus()+" "+msg.getSid()+" -> "+msg.getDid()+": "+msg.getMsg());
        //ack包
        msg.setCode((byte) 3);
        msg.setMsg("");
        writeQueue.put(msg);
    }

    private void processOfflineMsg(Message msg) throws InterruptedException {
        System.out.println("Offline message. status: "+msg.getStatus()+" "+msg.getSid()+" -> "+msg.getDid()+": "+msg.getMsg());
        //ack包
        msg.setCode((byte) 3);
        msg.setMsg("");
        writeQueue.put(msg);
    }

    private void process(Message msg) throws InterruptedException {
        switch (msg.getCode()){
            case 0://上线
                processOnlineMsg(msg);
                break;
            case 1://下线
                processOfflineMsg(msg);
                break;
            case 2://消息
                processMsg(msg);
                break;
            case 3://ack
                msg=ackTable.remove(msg.getHcode());
                if (msg!=null){
                    msg.setLastms(System.currentTimeMillis());
                    ackedTable.put(msg.getHcode(),msg);
                }
                break;
            case 4://重传
                if (!ackedTable.containsKey(msg.getHcode())){
                    processMsg(msg);
                }
                break;
            case 5://心跳
                processHeart(msg);
                break;
            default:
                break;
        }

    }

    @Override
    public void run() {
        System.out.println("UDPCliReadThread on.");
        byte[] bs=new byte[BUFSIZE];
        DatagramPacket dp=new DatagramPacket(bs,bs.length);
        Message msg;
        int bslen;
        try {
            while (true){
                ds.receive(dp);
                bslen=dp.getLength();
                if (bslen>=22){//小于22丢弃
                    msg=new Message(bs,bslen,CHARSET, (InetSocketAddress) dp.getSocketAddress());
                    process(msg);
                }
                dp.setLength(bs.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
