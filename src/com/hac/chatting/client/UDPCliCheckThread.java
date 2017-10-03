package com.hac.chatting.client;

import com.hac.chatting.DTO.Message;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPCliCheckThread implements Runnable {
    private final Hashtable<String,Message> ackTable,ackedTable;
    private final LinkedBlockingQueue<Message> writeQueue;

    public UDPCliCheckThread(Hashtable<String, Message> ackTable, Hashtable<String, Message> ackedTable, LinkedBlockingQueue<Message> writeQueue) {
        this.ackTable = ackTable;
        this.ackedTable=ackedTable;
        this.writeQueue = writeQueue;
    }

    @Override
    public void run() {
        System.out.println("UDPCliCheckThread on.");
        Iterator<Map.Entry<String,Message>> acks;
        Map.Entry<String,Message> ack;
        Message msg;
        while (true){
            try {
                Thread.sleep(500);
                long curms= System.currentTimeMillis();
                acks=ackTable.entrySet().iterator();
                synchronized (ackTable){
                    while(acks.hasNext()){
                        ack=acks.next();
                        msg=ack.getValue();
                        if (curms-msg.getLastms()>4000){//4s超时重传
                            //一般包重传
                            if (msg.getStatus()<6){//重传超过5次视为断线
                                msg.setCode((byte) 4);//重传码
                                msg.setStatus((byte) (msg.getStatus()+1));
                                writeQueue.put(msg);
                            }
                            msg.setCode((byte) 4);//重传
                            acks.remove();
                            writeQueue.put(msg);
                        }
                    }
                }
                acks=ackedTable.entrySet().iterator();
                synchronized (ackedTable){
                    while (acks.hasNext()){
                        ack=acks.next();
                        if (curms-ack.getValue().getLastms()>6000)acks.remove();//6s超时确认接收并从表删除
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
