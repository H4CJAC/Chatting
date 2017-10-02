package com.hac.chatting.client;

import com.hac.chatting.DTO.OP;
import com.hac.chatting.utils.CastUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Scanner;

import static com.hac.chatting.conf.UDPConf.BUFSIZE;
import static com.hac.chatting.conf.UDPConf.CHARSET;

public class UDPCClient implements Runnable{

    private volatile boolean stopped=false;
    private String serhost;
    private int serport;
    private Long sid,did;

    public UDPCClient(String sh, int serport, Long sid, Long did){
        serhost=sh;
        this.serport=serport;
        this.sid=sid;
        this.did=did;
    }

    public static void main(String[] args) {
        for(int i=0;i<1;i++){
            Thread t=new Thread(new UDPCClient("localhost",12346,3l,1l));
            t.start();
        }
    }

    public void halt(){
        stopped=true;
    }

    @Override
    public void run() {
        InetSocketAddress seraddr=new InetSocketAddress(serhost,serport);
        try (
                DatagramSocket ds=new DatagramSocket();
                ){
            byte[] bs=new byte[BUFSIZE];
            int bslen=OP.setBs(bs, (byte) 0,sid,did,new byte[]{});
            DatagramPacket dp=new DatagramPacket(bs,bslen,seraddr);
            ds.send(dp);
            dp.setLength(bs.length);
            ds.receive(dp);
            byte code=bs[0];
            Long fid= CastUtil.bs2l(bs,1);
            int remain=dp.getLength()-9;
            byte[] msgbs=new byte[remain];
            for(int i=0;i<remain;i++)msgbs[i]=bs[9+i];
            System.out.println("code: "+code+" fid: "+fid+" msg: "+new String(msgbs,CHARSET));
            UDPCCliReadThread rr=new UDPCCliReadThread(ds);
            Thread rt=new Thread(rr);
            rt.start();
            Scanner scanner=new Scanner(System.in);
            String input;
            while (!"quit".equals((input=scanner.nextLine()))){
                if (input.length()>500){
                    System.out.println("Message's size has to be no more than 500. Please input again: \n");
                    continue;
                }
                bslen=OP.setBs(bs, (byte) 1,sid,did,input.getBytes(CHARSET));
                dp.setLength(bslen);
                dp.setSocketAddress(seraddr);
                ds.send(dp);
            }
            rr.halt();
            rt.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
