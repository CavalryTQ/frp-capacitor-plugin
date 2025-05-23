package com.getcapacitor;

import static org.junit.Assert.*;

import android.os.Build;

import org.junit.Test;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

//    @Test
//    public void testPath() {
//        System.out.println("test");
//        System.out.println(LinuxISAPath.X86_64.getISAPath());
//
//        // 获取该项目路径
//        System.out.println(System.getProperty("user.dir"));
//    }

    @Test
    public void getPrimaryISA() {
        System.out.printf(Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown");
    }
}
