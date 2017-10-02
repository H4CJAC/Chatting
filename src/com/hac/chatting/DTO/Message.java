package com.hac.chatting.DTO;

import com.hac.chatting.utils.CastUtil;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import static com.hac.chatting.conf.UDPConf.CHARSET;

public class Message {
    private byte code;//0-上线，1-下线，2-消息，3-ack，4-重传,5-心跳
    private byte status;//0-成功，-1-失败
    private int hcode;
    private long sid;
    private long did;
    private String msg;
    private InetSocketAddress addr;
    private long lastms;

    public Message(byte[] bs, int length, Charset charset,InetSocketAddress addr){
        code=bs[0];
        status=bs[1];
        hcode=CastUtil.bs2i(bs,2);
        sid=CastUtil.bs2l(bs,6);
        did=CastUtil.bs2l(bs,14);
        msg=new String(bs,22,length-22,charset);
        this.addr=addr;
    }

    public int getBytes(byte[] bs,Charset charset){
        bs[0]=code;
        bs[1]=status;
        CastUtil.i2bs(hcode,bs,2);
        CastUtil.l2bs(sid,bs,6);
        CastUtil.l2bs(did,bs,14);
        byte[] msgbs=msg.getBytes(charset);
        CastUtil.bs2bs(msgbs,bs,22);
        return 22+msgbs.length;
    }

    public Message(byte code, byte status,int hcode, long sid, long did, String msg,InetSocketAddress addr) {
        this.code = code;
        this.status = status;
        this.hcode=hcode;
        this.sid = sid;
        this.did = did;
        this.msg = msg;
        this.addr=addr;
    }

    public long getLastms() {
        return lastms;
    }

    public void setLastms(long lastms) {
        this.lastms = lastms;
    }

    public int getHcode() {
        return hcode;
    }

    public void setHcode(int hcode) {
        this.hcode = hcode;
    }

    public InetSocketAddress getAddr() {
        return addr;
    }

    public void setAddr(InetSocketAddress addr) {
        this.addr = addr;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
