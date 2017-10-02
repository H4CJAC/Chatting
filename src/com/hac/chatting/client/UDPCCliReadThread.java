package com.hac.chatting.client;

import com.hac.chatting.utils.CastUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

import static com.hac.chatting.conf.UDPConf.BUFSIZE;
import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPCCliReadThread implements Runnable {
    private DatagramSocket ds;
    private volatile boolean stopped=false;

    public UDPCCliReadThread(DatagramSocket ds) {
        this.ds=ds;
    }

    public void halt(){
        stopped=true;
    }

    @Override
    public void run() {
        byte[] bs=new byte[BUFSIZE];
        DatagramPacket dp=new DatagramPacket(bs,bs.length);
        try {
            while (!stopped){
                ds.receive(dp);
                byte code=bs[0];
                Long fid= CastUtil.bs2l(bs,1);
                int remain=dp.getLength()-9;
                byte[] msgbs=new byte[remain];
                for (int i=0;i<remain;i++)msgbs[i]=bs[i+9];
                System.out.println("code: "+code+" fid: "+fid+" msg: "+new String(msgbs,CHARSET));
                dp.setLength(bs.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
