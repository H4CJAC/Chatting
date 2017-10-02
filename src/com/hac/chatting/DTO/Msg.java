package com.hac.chatting.DTO;

import com.hac.chatting.utils.CastUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Msg {
    private byte code;
    private Long fid;
    private String msg;

    public static void setBuffer(ByteBuffer buffer,byte code,Long fid,byte[] msg){
        buffer.put(code);
        buffer.putLong(fid);
        buffer.put(msg);
    }

    public static int setBs(byte[] bs,byte code,Long fid,byte[] msg){
        bs[0]=code;//消息码，0-成功，-1-失败
        CastUtil.l2bs(fid,bs,1);//源id，0为服务器
        CastUtil.bs2bs(msg,bs,9);//消息正体
        return 9+msg.length;
    }

    public byte[] getBytes(Charset charset){
        byte[] strbs=msg.getBytes(charset);
        byte[] bs=new byte[9+strbs.length];
        bs[0]=code;//消息码，0-成功，-1-失败
        CastUtil.l2bs(fid,bs,1);//源id，0为服务器
        CastUtil.bs2bs(msg.getBytes(charset),bs,9);//消息正体
        return bs;
    }

    public Msg(byte code, Long fid, String msg) {
        this.code = code;
        this.fid = fid;
        this.msg = msg;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public Long getFid() {
        return fid;
    }

    public void setFid(Long fid) {
        this.fid = fid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
