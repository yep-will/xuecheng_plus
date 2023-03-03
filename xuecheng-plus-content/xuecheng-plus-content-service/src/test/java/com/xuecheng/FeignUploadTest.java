package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author will
 * @version 1.0
 * @description 测试使用feign远程上传文件
 * @date 2023/3/2 20:09
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("E:\\z-xuecheng-test\\test.html"));
        String result = mediaServiceClient.upload(multipartFile, "course", "test.html");
        System.out.println(result);
    }

}
