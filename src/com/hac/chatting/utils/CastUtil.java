package com.hac.chatting.utils;

import com.hac.chatting.Exceptions.CastUtilException;

public class CastUtil {
    public static void i2bs(int i,byte[] bs,int offset){
        offset+=3;
        bs[offset--]= (byte) (i&0xff);i>>=8;
        bs[offset--]= (byte) (i&0xff);i>>=8;
        bs[offset--]= (byte) (i&0xff);i>>=8;
        bs[offset--]= (byte) (i&0xff);
    }
    public static int bs2i(byte[] bs,int offset) {
        int i=0;
        i|=(bs[offset++]&0xff);i<<=8;
        i|=(bs[offset++]&0xff);i<<=8;
        i|=(bs[offset++]&0xff);i<<=8;
        i|=(bs[offset++]&0xff);
        return i;
    }

    public static void l2bs(long l,byte[] bs,int offset){
        offset+=7;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);l>>=8;
        bs[offset--]= (byte) (l&0xff);
    }
    public static long bs2l(byte[] bs,int offset) {
        long l=0;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);l<<=8;
        l|=(bs[offset++]&0xff);
        return l;
    }
    public static void bs2bs(byte[] obs,byte[] tbs,int offset){
        for(byte b:obs){
            tbs[offset++]=b;
        }
    }

}
