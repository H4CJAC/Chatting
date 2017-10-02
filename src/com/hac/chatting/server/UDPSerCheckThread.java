package com.hac.chatting.server;

import com.hac.chatting.DTO.Message;

import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class UDPSerCheckThread implements Runnable {
    private final Hashtable<Integer,Message> ackTable,ackedTable;
    private final Hashtable<Long,InetSocketAddress> onlineTable;
    private final LinkedBlockingQueue<Message> writeQueue;

    public UDPSerCheckThread(Hashtable<Integer, Message> ackTable, Hashtable<Integer, Message> ackedTable, Hashtable<Long, InetSocketAddress> onlineTable, LinkedBlockingQueue<Message> writeQueue) {
        this.ackTable = ackTable;
        this.ackedTable=ackedTable;
        this.onlineTable = onlineTable;
        this.writeQueue = writeQueue;
    }

    private void iterSendOffline(long offlineId) throws InterruptedException {//下线消息群发
        Message msg;
        synchronized (onlineTable){
            Set<Map.Entry<Long,InetSocketAddress>> addrs=onlineTable.entrySet();
            for(Map.Entry<Long,InetSocketAddress> addr:addrs){
                msg=new Message((byte) 1,(byte) 0,0,offlineId,addr.getKey(),"",addr.getValue());
                msg.setHcode(msg.hashCode());
                writeQueue.put(msg);
            }
        }
    }

    @Override
    public void run() {
        System.out.println("UDPSerCheckThread on.");
        Iterator<Map.Entry<Integer,Message>> acks;
        Map.Entry<Integer,Message> ack;
        while (true){
            try {
                for (int i=0;i<50;i++){
                    Thread.sleep(500);
                    long curms= System.currentTimeMillis();
                    acks=ackTable.entrySet().iterator();
                    Message msg;
//                    System.out.println("ackTable size: "+ackTable.size());
                    synchronized (ackTable){
                        while(acks.hasNext()){
                            ack=acks.next();
                            msg=ack.getValue();
                            if (curms-msg.getLastms()>4000){//4s超时重传
                                if (msg.getCode()==5){//心跳包重传
                                    if (msg.getStatus()<5){
                                        msg.setStatus((byte) (msg.getStatus()+1));
                                        writeQueue.put(msg);
                                    }else {//超过5次视为断线
                                        onlineTable.remove(msg.getDid());
                                        iterSendOffline(msg.getDid());
                                        System.out.println(msg.getDid()+" is offline.");
                                    }
                                    acks.remove();
                                    continue;
                                }
                                //一般包重传
                                if (msg.getStatus()<6){//重传超过5次视为断线
                                    msg.setCode((byte) 4);//重传码
                                    msg.setStatus((byte) (msg.getStatus()+1));
                                    writeQueue.put(msg);
                                }
                                acks.remove();
                            }
                        }
                    }
                    acks=ackedTable.entrySet().iterator();
//                    System.out.println("ackedTable size: "+ackedTable.size());
                    synchronized (ackedTable){
                        while (acks.hasNext()){
                            ack=acks.next();
                            if (curms-ack.getValue().getLastms()>6000)acks.remove();//6s超时确认接收并从表删除
                        }
                    }
                }
                onlineTable.forEach((k,v)->{//遍历发心跳
                    Message msg=new Message((byte) 5,(byte) 0,0,0,k,"",v);
                    msg.setHcode(msg.hashCode());
                    try {
                        writeQueue.put(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
