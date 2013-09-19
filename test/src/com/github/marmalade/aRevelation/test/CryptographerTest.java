package com.github.marmalade.aRevelation.test;

import com.github.marmalade.aRevelation.Cryptographer;

import com.github.marmalade.aRevelation.FileEntriesFragment;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 6/30/13
 * Time: 11:32 AM
 */
public class CryptographerTest extends TestCase {

    private static final String DECRYPTED_DATA_FILE_4_14 = "test/res/rvl_test-0.4.14.xml";
    private static final String ENCRYPTED_DATA_FILE_4_14 = "test/res/rvl_test-0.4.14";


    @Test
    public void testDecrypt() {
        try {
            File file = new File(ENCRYPTED_DATA_FILE_4_14);
            FileInputStream input = new FileInputStream(DECRYPTED_DATA_FILE_4_14);
            byte[] fileData = new byte[input.available()];
            input.read(fileData);
            input.close();
            String expectedResult = new String(fileData, "UTF-8");
            assertEquals("Testing simple decrypt",expectedResult, Cryptographer.decrypt(file, "test"));
        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }

    }

    @Test
    public void testParsing() {
        try {
            FileInputStream input = new FileInputStream(DECRYPTED_DATA_FILE_4_14);
            byte[] fileData = new byte[input.available()];
            input.read(fileData);
            input.close();
            String inputData = new String(fileData, "UTF-8");
            FileEntriesFragment.Entry.parseDecryptedXml(inputData, new ArrayList<FileEntriesFragment.Entry>());
        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testEncrypt() {
        try {
            // Get data
            String xml = readFileAsString(DECRYPTED_DATA_FILE_4_14);

            byte[] encrypted = Cryptographer.encrypt(xml, "test");
            String decrypt = Cryptographer.decrypt(encrypted, "text");
            assertEquals(xml, decrypt);

        } catch (Exception e) {
            assert(false);
        }
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
