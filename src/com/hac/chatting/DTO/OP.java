package com.hac.chatting.DTO;

import com.hac.chatting.utils.CastUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class OP {
    private byte opcode;
    private Long sid;
    private Long did;
    private String msg;

    public static void setBuffer(ByteBuffer buffer,byte opcode,Long sid,Long did,byte[] msg){
        buffer.put(opcode);
        buffer.putLong(sid);
        buffer.putLong(did);
        buffer.put(msg);
    }

    public static int setBs(byte[] bs,byte opcode,Long sid,Long did,byte[] msg){
        bs[0]=opcode;//操作码，0-上线，1-聊天，2-下线
        CastUtil.l2bs(sid,bs,1);//源id，0为服务器
        CastUtil.l2bs(did,bs,9);//目的id，0为服务器
        CastUtil.bs2bs(msg,bs,17);//消息正体
        return 17+msg.length;
    }

    public byte[] getBytes(Charset charset){
        byte[] strbs=msg.getBytes(charset);
        byte[] bs=new byte[17+strbs.length];
        bs[0]=opcode;//操作码，0-上线，1-聊天，2-下线
        CastUtil.l2bs(sid,bs,1);//源id，0为服务器
        CastUtil.l2bs(did,bs,9);//目的id，0为服务器
        CastUtil.bs2bs(msg.getBytes(charset),bs,17);//消息正体
        return bs;
    }

    public OP(byte opcode,Long sid,Long did,String msg){
        this.opcode=opcode;
        this.sid=sid;
        this.did=did;
        this.msg=msg;
    }

    public byte getOpcode() {
        return opcode;
    }

    public void setOpcode(byte opcode) {
        this.opcode = opcode;
    }

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public Long getDid() {
        return did;
    }

    public void setDid(Long did) {
        this.did = did;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
