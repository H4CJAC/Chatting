package com.hac.chatting.DTO;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TCPMsg {
    private byte code;
    private byte status;
    private long uid;
    private String username;

    public TCPMsg(byte code, byte status, long uid, String username) {
        this.code = code;
        this.status = status;
        this.uid = uid;
        this.username = username;
    }

    public TCPMsg(ByteBuffer buffer, Charset charset){
        code=buffer.get();
        status=buffer.get();
        uid=buffer.getLong();
        byte[] bs=new byte[buffer.remaining()];
        buffer.get(bs);
        username=new String(bs,charset);
    }

    public void getBuffer(ByteBuffer buffer,Charset charset){
        buffer.put(code);
        buffer.put(status);
        buffer.putLong(uid);
        buffer.put(username.getBytes(charset));
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

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
