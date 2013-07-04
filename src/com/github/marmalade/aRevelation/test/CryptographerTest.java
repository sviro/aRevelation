package com.github.marmalade.aRevelation.test;

import com.github.marmalade.aRevelation.Cryptographer;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 6/30/13
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class CryptographerTest extends TestCase {
    public void testEncrypt() {
        File file = new File("test/rvl_test-0.4.14");

        try {
            Cryptographer.encrypt(file);
        }
        catch (Exception entt){
            entt.printStackTrace();
            fail("WHOOPS! Threw ExceptionNotToThrow" + entt.toString());
        }
        catch (Throwable t){
            //do nothing since other exceptions are OK
        }
        assertTrue(true);
    }

}
