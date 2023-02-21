package com.xuecheng.media;

import io.minio.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

/**
 * @author will
 * @version 1.0
 * @description 测试minio上传文件, 删除文件, 查询文件
 * @date 2023/2/16 16:18
 */
public class MinIOTest {

    //指定客户端
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();


    //上传
    @Test
    public void upload() {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("壁纸1.jpg")//同一个桶内对象名不能重复
                    .filename("F:\\图片\\本机照片\\壁纸\\壁纸1.jpg")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
            //浏览器直接访问 http://localhost:9000/testbucket/壁纸1.jpg
        } catch (Exception e) {
            System.out.println("上传失败");
        }

    }


    //指定桶内的子目录
    @Test
    public void upload2() {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/刘亦菲.jpg")//同一个桶内对象名不能重复
                    .filename("F:\\图片\\本机照片\\壁纸\\刘亦菲.jpg")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
        } catch (Exception e) {
            System.out.println("上传失败");
        }

    }


    //删除文件
    @Test
    public void delete() {
        try {
            RemoveObjectArgs removeObjectArgs =
                    RemoveObjectArgs.builder().bucket("testbucket").object("test/刘亦菲.jpg").build();
            minioClient.removeObject(removeObjectArgs);
            System.out.println("删除成功");
        } catch (Exception e) {
            System.out.println("删除失败");
        }

    }


    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("壁纸1.jpg").build();
        try (
                //这种写法, try完以后就直接关闭流了
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream =
                        new FileOutputStream(new File("C:\\Users\\haha\\OneDrive\\桌面\\新建文件夹\\壁纸1.jpg"));
        ) {
            if (inputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
            System.out.println("查询成功");
        } catch (Exception e) {
            System.out.println("查询失败");
        }

    }

}