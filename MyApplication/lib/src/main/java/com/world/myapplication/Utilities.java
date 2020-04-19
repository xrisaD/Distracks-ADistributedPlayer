package com.world.myapplication;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilities {
    private MessageDigest digester;
    public Utilities() throws NoSuchAlgorithmException {digester= MessageDigest.getInstance("MD5");}

    public static BigInteger getMd5(String input) {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(messageDigest);

            return  no;
        }
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger getMd5(byte[] input) {
        digester.update(input);
        byte[] digest = digester.digest();
        BigInteger no = new BigInteger(1, digest);
        return  no;
    }
}
