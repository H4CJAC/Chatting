package com.hac.chatting.DTO;

import com.hac.chatting.utils.CastUtil;
import com.hac.chatting.utils.SHA;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class Message {
    private final static Charset HCODECST=Charset.forName("latin1");

    private byte code;//0-上线，1-下线，2-消息，3-ack，4-重传,5-心跳
    private byte status;//0-成功，-1-失败
    private String hcode;//sha-256，32字节
    private long sid;
    private long did;
    private String msg;
    private InetSocketAddress addr;
    private long lastms;

    public Message(byte[] bs, int length, Charset charset,InetSocketAddress addr){
        code=bs[0];
        status=bs[1];
        hcode=new String(bs,2,32,HCODECST);
        sid=CastUtil.bs2l(bs,34);
        did=CastUtil.bs2l(bs,42);
        msg=new String(bs,50,length-50,charset);
        this.addr=addr;
    }

    public int getBytes(byte[] bs,Charset charset){
        bs[0]=code;
        bs[1]=status;
        byte[] hcodebs=hcode.getBytes(HCODECST);
        CastUtil.bs2bs(hcodebs,bs,2);
        CastUtil.l2bs(sid,bs,34);
        CastUtil.l2bs(did,bs,42);
        byte[] msgbs=msg.getBytes(charset);
        CastUtil.bs2bs(msgbs,bs,50);
        return 50+msgbs.length;
    }

    public Message(byte code, byte status, long sid, long did, String msg,InetSocketAddress addr,Charset charset) {
        this.code = code;
        this.status = status;
        this.sid = sid;
        this.did = did;
        this.msg = msg;
        this.addr=addr;
        byte[] msgbs=msg.getBytes(charset);
        byte[] bs=new byte[18+msgbs.length];
        bs[0]=code;
        bs[1]=status;
        CastUtil.l2bs(sid,bs,2);
        CastUtil.l2bs(did,bs,10);
        CastUtil.bs2bs(msgbs,bs,18);
        this.hcode=new String(SHA.getSHA256Bs(bs),HCODECST);
    }

    public long getLastms() {
        return lastms;
    }

    public void setLastms(long lastms) {
        this.lastms = lastms;
    }

    public String getHcode() {
        return hcode;
    }

    public void setHcode(String hcode) {
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
