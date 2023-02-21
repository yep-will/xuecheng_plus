package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大文件分块、合并
 * @date 2022/10/14 10:48
 */
public class BigFileTest {

    // 测试分块存储
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("F:/图片/本机照片/《老友记第六季 第10集》.mp4");

        // 分块文件存储路径
        File chunkFolderPath = new File("F:\\图片\\本机照片\\xuecheng-plus-test\\");
        if (!chunkFolderPath.exists()) {
            chunkFolderPath.mkdirs();
        }

        // 分块的大小 1024:1k, 再乘1024就是1m, 再乘50就是50m
        int chunkSize = 1024 * 1024 * 50;

        // 分块数量(向上转型)
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        // 思路，使用流对象读取源文件，向分块文件写数据，达到分块大小不再写。
        // RandomAccessFile是Java 输入/输出流体系中功能最丰富的文件内容访问类，
        // 它提供了众多的方法来访问文件内容，它既可以读取文件内容，也可以向文件
        // 输出数据。与普通的输入/输出流不同的是，RandomAccessFile支持"随机访问"
        // 的方式，程序可以直接跳转到文件的任意地方来读写数据。
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] b = new byte[1024];
        for (long i = 0; i < chunkNum; i++) {
            File file = new File("F:\\图片\\本机照片\\xuecheng-plus-test\\" + i);
            //如果分块文件存在，则删除
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                //向分块文件写数据流对象
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    //向文件中写数据
                    raf_write.write(b, 0, len);
                    //达到分块大小不再写了
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                raf_write.close();
            }
        }
        raf_read.close();
    }


    // 测试合并
    @Test
    public void testMerge() throws IOException {
        // 源文件
        File sourceFile = new File("F:/图片/本机照片/《老友记第六季 第10集》.mp4");

        // 分块文件存储路径
        File chunkFolderPath = new File("F:\\图片\\本机照片\\xuecheng-plus-test\\");
        if (!chunkFolderPath.exists()) {
            chunkFolderPath.mkdirs();
        }

        // 合并后的文件
        File mergeFile = new File("F:/图片/本机照片/xuecheng《老友记第六季 第10集》.mp4");
        mergeFile.createNewFile();

        // 思路，使用流对象读取分块文件，按顺序将分块文件依次向合并文件写数据
        // 获取分块文件列表,按文件名升序排序
        File[] chunkFiles = chunkFolderPath.listFiles();
        // 转成list集合进行排序
        List<File> chunkFileList = Arrays.asList(chunkFiles);
        // 按文件名升序排序, 构建比较器
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        // 创建合并文件的流对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        byte[] b = new byte[1024];
        for (File file : chunkFileList) {
            // 读取分块文件的流对象
            RandomAccessFile raf_read = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                // 向合并文件写数据
                raf_write.write(b, 0, len);
            }
        }

        // 校验合并后的文件是否正确, 使用md5校验
        FileInputStream sourceFileStream = new FileInputStream(sourceFile);
        FileInputStream mergeFileStream = new FileInputStream(mergeFile);
        String sourceMd5Hex = DigestUtils.md5Hex(sourceFileStream);
        String mergeMd5Hex = DigestUtils.md5Hex(mergeFileStream);
        if (sourceMd5Hex.equals(mergeMd5Hex)) {
            System.out.println("合并成功");
        }
    }


    // 查看源文件与分块文件的md5值是否相等
    @Test
    public void testMerge2() throws IOException {
        // 源文件
        File sourceFile = new File("F:/图片/本机照片/《老友记第六季 第10集》.mp4");

        // 分块文件存储路径
        File chunkFolderPath = new File("F:\\图片\\本机照片\\xuecheng-plus-test\\");
        if (!chunkFolderPath.exists()) {
            chunkFolderPath.mkdirs();
        }

        // 思路，使用流对象读取分块文件，按顺序将分块文件依次向合并文件写数据
        // 获取分块文件列表
        File[] chunkFiles = chunkFolderPath.listFiles();

        int i = 1;
        for (File file : chunkFiles) {
            FileInputStream fileInputStream = new FileInputStream(file);
            String md5 = DigestUtils.md5Hex(fileInputStream);
            System.out.println(i++ + ": " + md5);
        }
        FileInputStream sourceFileStream = new FileInputStream(sourceFile);
        String sourceMd5Hex = DigestUtils.md5Hex(sourceFileStream);
        System.out.println("源文件md5值: " + sourceMd5Hex);
    }


    @Test
    public void testGetChunkFileFolderPath() {
        String fileMd5 = "12sdfsgfsaggsdfagserwertew";
        String newString = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
        System.out.println(newString);

        System.out.println("123".substring(1, 2));
    }


    // 测试获取本地文件的绝对路径
    @Test
    public void testGetAbsolutePath() {
        try {
            File file = File.createTempFile("xuecheng-plus-test", "mp4");
            System.out.println(file.getAbsolutePath());
        } catch (IOException e) {

        }
    }

}
