package com.hac.chatting.server;

import com.hac.chatting.DTO.Message;
import com.hac.chatting.utils.CastUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hac.chatting.conf.UDPConf.BUFSIZE;
import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPSerReadThread implements Runnable {

    private final LinkedBlockingQueue<Message> writeQueue;
    private final Hashtable<Integer,Message> ackTable,ackedTable;
    private final Hashtable<Long,InetSocketAddress> onlineTable;
    private final DatagramSocket ds;

    public UDPSerReadThread(LinkedBlockingQueue<Message> writeQueue,Hashtable<Integer, Message> ackTable, Hashtable<Integer,Message> ackedTable
            ,Hashtable<Long,InetSocketAddress> onlineTable, DatagramSocket ds) {
        this.writeQueue=writeQueue;
        this.ackTable = ackTable;
        this.ackedTable=ackedTable;
        this.onlineTable=onlineTable;
        this.ds = ds;
    }

    private void processMsg(Message msg) throws InterruptedException {
        InetSocketAddress taddr=onlineTable.get(msg.getDid());
        Message tmsg;
        if (taddr==null){
            //回复不在线
            tmsg=new Message((byte) 2, (byte) 0,0,msg.getSid(),msg.getDid(),"target not online.",msg.getAddr());
            tmsg.setHcode(tmsg.hashCode());
        }else {
            //转发包
            tmsg=new Message((byte) 2, (byte) 0,0,msg.getSid(),msg.getDid(),msg.getMsg(),taddr);
            tmsg.setHcode(tmsg.hashCode());
        }
        System.out.println("status: "+tmsg.getStatus()+" "+tmsg.getSid()+" -> "+tmsg.getDid()+": "+tmsg.getMsg());
        //ack包
        msg.setCode((byte) 3);
        msg.setMsg("");
        writeQueue.put(msg);
        writeQueue.put(tmsg);
    }

    private void iterSendOnline(long onlineId,InetSocketAddress myaddr) throws InterruptedException {//上线消息群发并发送在线列表给刚上线用户
        Message msg;
        Set<Map.Entry<Long,InetSocketAddress>> addrs=onlineTable.entrySet();
        StringBuilder sbd=new StringBuilder();
        synchronized (onlineTable){
            for(Map.Entry<Long,InetSocketAddress> addr:addrs){
                msg=new Message((byte) 0,(byte) 0,0,onlineId,addr.getKey(),"",addr.getValue());
                msg.setHcode(msg.hashCode());
                writeQueue.put(msg);
                sbd.append(addr.getKey()+";");
            }
        }
        msg=new Message((byte) 0,(byte) 0,0,0,onlineId,sbd.toString(),myaddr);
        msg.setHcode(msg.hashCode());
        writeQueue.put(msg);
    }

    private void processHeart(Message msg) throws InterruptedException {
        System.out.println("Heartbreak. status: "+msg.getStatus()+" "+msg.getSid()+" -> "+msg.getDid()+": "+msg.getMsg());
        //ack包
        msg.setCode((byte) 3);
        msg.setMsg("");
        writeQueue.put(msg);
    }

    private void process(Message msg) throws InterruptedException {
        switch (msg.getCode()){
            case 0://上线
                iterSendOnline(msg.getSid(),msg.getAddr());
                onlineTable.put(msg.getSid(),msg.getAddr());
                //将ack放入写队列
                msg.setCode((byte) 3);//ack
                msg.setMsg("");
                writeQueue.put(msg);
                System.out.println(msg.getSid()+" "+msg.getAddr().getHostName()+": "+msg.getAddr().getPort()+" is online.");
                break;
            case 1://下线
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
                if (!ackedTable.contains(msg.getHcode())){
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
        System.out.println("UDPSerReadThread on.");
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
