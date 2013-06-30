package com.github.marmalade.aRevelation;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 6/26/13
 * Time: 2:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class Cryptographer {

    final static byte[] MAGIC_STRING_DATA_VERSION_2 = new byte[] {'r', 'v', 'l', 0, 2, 0};
    final static byte[] VERSION_0_4_7 = new byte[] {0, 4, 7};

    static String encrypt(File file) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] header = new byte[74];
        new DataInputStream(new FileInputStream(file)).readFully(header);

        byte[] iv = null;
        byte[] salt = null;

        if(Arrays.equals(Arrays.copyOfRange(header, 0, 6), MAGIC_STRING_DATA_VERSION_2)) {
            if(Arrays.equals(VERSION_0_4_7, Arrays.copyOfRange(header, 6, 9))) {
                salt = Arrays.copyOfRange(header, 12, 20);
                iv = Arrays.copyOfRange(header, 20, 36);
                Cipher cypher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKeyFactory scf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec ks = new PBEKeySpec("test".toCharArray(), salt, 12000, 256);
                SecretKey s = scf.generateSecret(ks);                                           // Bottleneck (12k)
                Key k = new SecretKeySpec(s.getEncoded(),"AES");
                cypher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));

                RandomAccessFile f = new RandomAccessFile(file.getAbsoluteFile(), "r");
                byte[] data = new byte[(int)f.length()];
                f.read(data);
                data = Arrays.copyOfRange(data, 32, (int)f.length() - 1);
                cypher.doFinal(data);


                String sx = "";
            }
        }

        return null;
    }

    private static String bytArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(byte b: a)
            sb.append(String.format("%02x", b&0xff));
        return sb.toString();
    }
}
