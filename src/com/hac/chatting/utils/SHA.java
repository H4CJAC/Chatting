package com.hac.chatting.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {
    private static MessageDigest SHA256Dig;
    private static MessageDigest SHA1Dig;

    static {
        try {
            SHA256Dig=MessageDigest.getInstance("sha-256");
            SHA1Dig=MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getSHA256Bs(byte[] bs){
        SHA256Dig.update(bs);
        return SHA256Dig.digest();
    }

    public static byte[] getMD5Bs(byte[] bs){
        SHA1Dig.update(bs);
        return SHA1Dig.digest();
    }

}
