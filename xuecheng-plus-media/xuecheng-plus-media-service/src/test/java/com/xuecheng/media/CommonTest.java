package com.xuecheng.media;

import org.junit.jupiter.api.Test;

/**
 * @author will
 * @version 1.0
 * @description 通用方法测试
 * @date 2023/2/24 11:50
 */
public class CommonTest {

    @Test
    public void testSplitString(){
        String fileName = "messi.avi";

        String[] parts = fileName.split("\\.");

        for(String part : parts){
            System.out.println(part);
        }

        String prefix = parts[0];
        String newFileName = prefix + ".mp4";
        System.out.println(newFileName);

    }

}
