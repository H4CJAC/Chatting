package com.hac.chatting.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CServer {

    private final static int PORT=12345;
    private final static int LOGINTHREADS=5;
    private final static int BUFFERSIZE=128;
    private final static Charset CHARSET=Charset.forName("utf8");
    private final static HashMap<Long,InetSocketAddress> map=new HashMap<>(1000);

    public static void main(String[] args) {

        try (
                Selector selector=Selector.open();
                ServerSocketChannel ssc=ServerSocketChannel.open();
                ){
            ExecutorService loginPool= Executors.newFixedThreadPool(LOGINTHREADS);
            ssc.configureBlocking(false);
            ServerSocket ssock=ssc.socket();
            ssock.bind(new InetSocketAddress(PORT));
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while(true){
                selector.select();
                Set<SelectionKey> skeys= selector.selectedKeys();
                for(SelectionKey sk:skeys){
                    if(sk.isAcceptable()){//accept连接
                        ServerSocketChannel sschannel=(ServerSocketChannel)sk.channel();
                        SocketChannel sc=sschannel.accept();
                        sc.configureBlocking(false);
                        sc.register(selector,SelectionKey.OP_READ);
                        System.out.println(sc.getRemoteAddress()+" is Connecting");
                    }else {
                        if(sk.isReadable()){//拉
                            //登陆
                            SocketChannel sc= (SocketChannel) sk.channel();
                            sk.interestOps(SelectionKey.OP_WRITE);
                            byte[] bs=new byte[BUFFERSIZE];
                            ByteBuffer buffer=ByteBuffer.wrap(bs);
                            StringBuilder sbd=new StringBuilder();
                            int n;
                            while((n=sc.read(buffer))>-1){
                                //？会读单数个么
                                buffer.flip();
                                buffer.get(bs,0,n);
                                buffer.clear();
                                sbd.append(new String(bs,CHARSET));
                            }
                            String msg=sbd.toString();//空格分隔，[操作 id 目的id]
                            String[] msgarr=msg.split(" ");
                            if(msgarr.length!=3){
                                try {
                                    int opcode=Integer.parseInt(msgarr[0]);
                                    Long id = Long.valueOf(msgarr[1]);
                                    switch (opcode){
                                        case 0://登陆
                                            //登陆判断
                                            //......
                                            map.put(id, (InetSocketAddress) sc.getRemoteAddress());
                                            //加入
                                            break;
                                        case 1://发起聊天...检查目标id是否存在，建立p2p连接。。。先画个流程图
                                        default:
                                            break;
                                    }
                                }catch (NumberFormatException nfe){
                                    sk.attach("-1");
                                }
                            }else sk.attach("-1");

                        }
                        if(sk.isWritable()){//推
                            String atch;
                            if ((atch= (String) sk.attachment())!=null){
                                SocketChannel sc= (SocketChannel) sk.channel();
                                byte[] bs=atch.getBytes(CHARSET);
                                sc.write(ByteBuffer.wrap(bs));
                            }
                            sk.cancel();
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
