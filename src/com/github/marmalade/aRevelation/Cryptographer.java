package com.github.marmalade.aRevelation;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 6/26/13
 * Time: 2:44 AM
 */
public class Cryptographer {

    final static byte[] MAGIC_STRING_DATA_VERSION_2 = new byte[] {'r', 'v', 'l', 0, 2, 0};
    final static byte[] VERSION_0_4_7 = new byte[] {0, 4, 7};

    /**
     * Main decpypt method
     * @param file
     * @param password
     * @return
     * @throws Exception
     */
    public static String decrypt(File file, String password) throws Exception {
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
                KeySpec ks = new PBEKeySpec(password.toCharArray(), salt, 12000, 256);
                SecretKey s = scf.generateSecret(ks);                                           // Bottleneck (12k)
                Key k = new SecretKeySpec(s.getEncoded(),"AES");
                cypher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));

                RandomAccessFile f = new RandomAccessFile(file.getAbsoluteFile(), "r");
                byte[] fileData = new byte[(int)f.length()];
                f.read(fileData);
                byte[] input = Arrays.copyOfRange(fileData, 36, (int)f.length());
                byte[] compressedData = cypher.doFinal(input);

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(compressedData);
                if(Arrays.equals(md.digest(), Arrays.copyOfRange(input, 0, 36))) {
                    throw new Exception("Invalid data");
                }

                byte[] result = unzipByteArray(Arrays.copyOfRange(compressedData,32, compressedData.length));
                return new String(result);
            }
        }

        throw new Exception("Unknown file format");
    }

    private static String bytArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(byte b: a)
            sb.append(String.format("%02x", b&0xff));
        return sb.toString();
    }

    private static byte[] unzipByteArray(byte[] inputData) throws DataFormatException, IOException {
        Inflater decompressor = new Inflater();
        decompressor.setInput(inputData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(inputData.length);
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            int count = decompressor.inflate(buf);
            bos.write(buf, 0, count);
        }
        bos.close();
        String outString = new String(bos.toByteArray());
        return bos.toByteArray();
    }


}
