package test;


import sun.security.provider.SHA;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;

public class Test {
    public static void main(String[] args) {
        try {
            MessageDigest md=MessageDigest.getInstance("sha-256");
            md.update("test".getBytes());
            byte[] res=md.digest();
            System.out.println(res.length);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
