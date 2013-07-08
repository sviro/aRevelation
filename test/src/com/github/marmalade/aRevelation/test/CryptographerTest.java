package com.github.marmalade.aRevelation.test;

import com.github.marmalade.aRevelation.Cryptographer;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Author: <a href="mailto:alexey.kislin@gmail.com">Alexey Kislin</a>
 * Date: 6/30/13
 * Time: 11:32 AM
 */
public class CryptographerTest extends TestCase {

    @Test
    public void testEncrypt() {
        try {
            File file = new File("test/res/rvl_test-0.4.14");
            FileInputStream input = new FileInputStream("test/res/rvl_test-0.4.14.xml");
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

}
