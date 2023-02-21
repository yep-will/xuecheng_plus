package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.CommonUtils;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 媒资文件管理业务接口实现类
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    /**
     * 普通文件存储桶
     */
    @Value("${minio.bucket.files}")
    private String bucket_files;

    /**
     * 视频文件存储桶
     */
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    /**
     * 解决addMediaFilesToDb的事务控制问题，通过代理对象去调用方法
     */
    @Autowired
    MediaFileService currentProxy;


    /**
     * @param companyId           机构id
     * @param pageParams          页码参数
     * @param queryMediaParamsDto 查询参数
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 查询媒资列表
     * @author will
     * @date 2023/2/17 12:38
     */
    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId,
                                                  PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件：根据文件名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        //构建查询条件：根据文件类型查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());

        return mediaListResult;
    }


    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 上传文件信息
     * @param bytes               文件字节数组，实现调用这个接口时和框架无关，更加通用
     * @param folder              桶下边的子目录,如果不传则默认年、月、日
     * @param objectName          存储在minIO上的路径名
     * @return com.xuecheng.media.model.dto.UploadFileResultDto
     * @description 上传文件的 ！通用接口！
     * @author will
     * @date 2023/2/16 21:57
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId,
                                          UploadFileParamsDto uploadFileParamsDto,
                                          byte[] bytes,
                                          String folder,
                                          String objectName) {

        if (StringUtils.isEmpty(folder)) {
            //如果没有传递目录路径, 自动生成目录的路径 按年月日生成，
            folder = getFileFolder(new Date(), true, true, true);
        } else if (!folder.endsWith("/")) {
            //!folder.endsWith("/")：字符串不是以/结尾
            //如果传递了目录路径, 由于目录后边必须要有'/',
            folder = folder + "/";
        }

        //文件名称
        String filename = uploadFileParamsDto.getFilename();
        //得到文件的md5值
        String fileMd5 = DigestUtils.md5Hex(bytes);
        if (StringUtils.isEmpty(objectName)) {
            //如果objectName为空，使用文件的md5值为objectName，不要漏了文件扩展名（找到最后一个.后面的字符串）
            //filename：原本的文件名;  objectname：存储在minIO上的完整路径名称    二者不同
            objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
        }
        //objectName:存储在minio的名称
        objectName = folder + objectName;

        try {
            //将文件上传到分布式文件系统
            addMediaFilesToMinIO(bytes, bucket_files, objectName);
            //保存至数据库
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);

            //准备返回数据
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;

        } catch (Exception e) {
            log.debug("上传文件失败：{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        //return null;
    }


    /**
     * @param bytes      文件字节数组
     * @param bucket     桶目录
     * @param objectName 存储在minIO上的路径名
     * @return void
     * @description 将文件上传到分布式文件系统
     * @author will
     * @date 2023/2/17 12:40
     */
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {
        //资源的媒体类型（默认为未知的二进制流）
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        if (objectName.indexOf(".") >= 0) {
            //取objectName中的文件扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            //获取相应的资源媒体类型
            contentType = new CommonUtils().getMimeTypeByExtension(extension);
        }

        try {
            //将字节数组转换程内存流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            //构造参数
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    //stream参数说明
                    //InputStream stream; long objectSize 对象大小; long partSize 分片大小(-1表示5M,最大不要超过5T，最多10000)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            //上传到minio
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("上传文件到文件系统出错:{}", e.getMessage());
            XueChengPlusException.cast("上传文件到文件系统出错");
        }
    }


    /**
     * @param filePath   文件绝对路径
     * @param bucket     桶目录
     * @param objectName 存储在minIO上的路径名
     * @return void
     * @description 将文件上传到文件系统(重写)
     * @author will
     * @date 2023/2/21 15:58
     */
    private void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("文件上传成功:{}", filePath);
        } catch (Exception e) {
            XueChengPlusException.cast("文件上传到文件系统失败");
        }
    }


    /**
     * @param companyId           机构id
     * @param fileId              文件id
     * @param uploadFileParamsDto 上传文件参数对象
     * @param bucket              桶目录
     * @param objectName          存储在minIO上的路径名
     * @return com.xuecheng.media.model.po.MediaFiles
     * @description 保存到数据库（这里需要事务控制）
     * @author will
     * @date 2023/2/17 12:45
     */
    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileId,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket,
                                        String objectName) {
        //保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();

            //封装数据
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            // 获取扩展名
            String extension = null;
            String filename = uploadFileParamsDto.getFilename();
            if (StringUtils.isNotEmpty(filename) && filename.contains(".")) {
                extension = filename.substring(filename.lastIndexOf("."));
            }
            // 获取媒体类型
            String mimeType = new CommonUtils().getMimeTypeByExtension(extension);
            // 只有图片.mp4格式文件可以设置url, 否则设置为null后期处理
            if (mimeType.contains("image") || mimeType.contains("mp4")) {
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }

            //插入文件表
            mediaFilesMapper.insert(mediaFiles);
            //抛出异常, 制造异常
            //int i = 1 / 0;
        }
        return mediaFiles;
    }


    /**
     * @param date  日期
     * @param year  年, 如果传递则有年份文件夹
     * @param month 月
     * @param day   日
     * @return java.lang.String
     * @description 根据日期拼接目录
     * @author will
     * @date 2023/2/17 12:44
     */
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前日期字符串
        String dateString = sdf.format(new Date());
        //取出年、月、日
        String[] dateStringArray = dateString.split("-");

        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]).append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]).append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]).append("/");
        }
        return folderString.toString();
    }


    /**
     * @param fileMd5 文件的md5
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查文件是否存在
     * @author will
     * @date 2023/2/20 20:45
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 在文件表存在，并且在文件系统存在，此文件才存在

        // 查看是否在文件表数据库中存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (null == mediaFiles) {
            return RestResponse.success(false);
        }

        // 查看是否在文件系统存在
        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()
                .bucket(mediaFiles.getBucket())
                .object(mediaFiles.getFilePath())
                .build();
        try {
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            if (null == inputStream) {
                // 文件不存在
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            // 文件不存在
            return RestResponse.success(false);
        }
        // 文件已存在
        return RestResponse.success(true);
    }


    /**
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查分块是否存在
     * @author will
     * @date 2023/2/20 20:46
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 得到分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        // 查询文件系统分块文件是否存在
        // 查看是否在文件系统存在
        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()
                .bucket(bucket_videofiles)
                .object(chunkFilePath)
                .build();
        try {
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            if (null == inputStream) {
                // 文件块不存在
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            // 文件块不存在
            return RestResponse.success(false);
        }
        // 文件块已存在
        return RestResponse.success(true);
    }


    /**
     * @param fileMd5 文件的Mmd5
     * @return java.lang.String
     * @description 得到分块文件的目录
     * @author will
     * @date 2023/2/20 21:02
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    /**
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @param bytes   分块文件字节
     * @return com.xuecheng.base.model.RestResponse
     * @description 上传分块
     * @author will
     * @date 2023/2/20 21:41
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        // 得到分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        try {
            // 将分块上传到文件系统
            addMediaFilesToMinIO(bytes, bucket_videofiles, chunkFilePath);
            // 上传成功
            return RestResponse.success(true);
        } catch (Exception e) {
            log.debug("上传分块文件失败：{}", e.getMessage());
            return RestResponse.validfail(false, "上传分块失败");
        }
    }


    /**
     * @param companyId           机构id
     * @param fileMd5             源文件md5值
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @description 合并分块
     * @author will
     * @date 2023/2/21 15:24
     */
    @Override
    public RestResponse mergechunks(Long companyId,
                                    String fileMd5,
                                    int chunkTotal,
                                    UploadFileParamsDto uploadFileParamsDto) {
        // 下载分块(已经排好顺序了)
        File[] chunkFiles = downloadChunks(fileMd5, chunkTotal);

        // 得到合并后文件的扩展名
        // 获取文件名
        String filename = uploadFileParamsDto.getFilename();
        // 获取扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        File tempMergeFile = null;
        try {
            try {
                // 创建一个临时文件作为合并文件
                tempMergeFile = File.createTempFile("'merge'", extension);
            } catch (IOException e) {
                XueChengPlusException.cast("创建临时合并文件出错");
            }

            // 创建合并文件的流对象
            try (RandomAccessFile raf_write = new RandomAccessFile(tempMergeFile, "rw")) {
                byte[] b = new byte[1024];
                for (File file : chunkFiles) {
                    // 读取分块文件的流对象
                    try (RandomAccessFile raf_read = new RandomAccessFile(file, "r")) {
                        int len = -1;
                        while ((len = raf_read.read(b)) != -1) {
                            // 向合并文件写数据
                            raf_write.write(b, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                XueChengPlusException.cast("合并文件过程出错");
            }

            // 校验合并后的文件是否正确
            try {
                FileInputStream mergeFileStream = new FileInputStream(tempMergeFile);
                String mergeMd5Hex = DigestUtils.md5Hex(mergeFileStream);
                if (!fileMd5.equals(mergeMd5Hex)) {
                    log.debug("合并文件校验不通过, 文件路径:{}, 原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                    XueChengPlusException.cast("合并文件校验不通过");
                }
            } catch (IOException e) {
                log.debug("合并文件校验程序出错, 文件路径:{}, 原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                XueChengPlusException.cast("合并文件校验程序出错");
            }

            // 拿到合并文件在minio的存储路径
            String mergeFilePath = getFilePathByMd5(fileMd5, extension);
            // 将合并后的文件上传到文件系统
            // 拿到字节数组转成内存流比较消耗内存，所以这里重写addMediaFilesToMinIO方法
            addMediaFilesToMinIO(tempMergeFile.getAbsolutePath(), bucket_videofiles, mergeFilePath);

            // 将文件信息入库保存
            // 设置合并文件大小
            uploadFileParamsDto.setFileSize(tempMergeFile.length());
            addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, mergeFilePath);

            return RestResponse.success(true);
        } finally {
            // 删除临时分块文件
            if (null != chunkFiles) {
                for (File chunkFile : chunkFiles) {
                    if (chunkFile.exists()) {
                        chunkFile.delete();
                    }
                }
            }
            // 删除合并的临时文件
            if (null != tempMergeFile) {
                tempMergeFile.delete();
            }
        }
    }


    /**
     * @param id 文件id
     * @return com.xuecheng.media.model.po.MediaFiles 文件信息
     * @description 根据id查询文件信息
     * @author will
     * @date 2023/2/21 23:42
     */
    @Override
    public MediaFiles getFileById(String id) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(id);
        if (null == mediaFiles) {
            XueChengPlusException.cast("文件不存在");
        }
        String url = mediaFiles.getUrl();
        if (StringUtils.isEmpty(url)) {
            XueChengPlusException.cast("文件还没有处理, 请稍后预览");
        }

        return mediaFiles;
    }


    /**
     * @param fileMd5    源文件md5值
     * @param chunkTotal 分块数量
     * @return java.io.File[]  分块文件数组, 已经排好顺序
     * @description 下载分块
     * @author will
     * @date 2023/2/21 15:02
     */
    private File[] downloadChunks(String fileMd5, int chunkTotal) {
        // 得到分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 用来存储结果的分块文件数组
        File[] chunkFiles = new File[chunkTotal];
        // 开始下载
        for (int i = 0; i < chunkTotal; i++) {
            // 分块文件的路径
            String chunkFilePath = chunkFileFolderPath + i;
            // 分块文件
            File chunkFile = null;
            try {
                // 创建临时文件存储分块内容
                // File.createTempFile：在默认临时文件目录中创建一个空文件，使用给定的前缀
                // 和后缀生成其名称。调用此方法等效于调用 createTempFile（前缀、后缀、null）。
                chunkFile = File.createTempFile("chunk", null);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("创建分块临时文件出错" + e.getMessage());
            }
            // 下载分块文件
            downloadFileFromMinIO(chunkFile, bucket_videofiles, chunkFilePath);
            chunkFiles[i] = chunkFile;
        }
        return chunkFiles;
    }


    /**
     * @param file       承接文件对象
     * @param bucket     桶目录
     * @param objectName 系统存储文件目录
     * @return java.io.File
     * @description 根据桶和文件路径从minio下载文件
     * @author will
     * @date 2023/2/21 15:02
     */
    public File downloadFileFromMinIO(File file, String bucket, String objectName) {
        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()
                .bucket(bucket)
                .object(objectName)
                .build();
        try (
                InputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(file);
        ) {
            // 拷贝流
            IOUtils.copy(inputStream, outputStream);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("查询文件出错");
        }
        return null;
    }


    /**
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return java.lang.String
     * @description 获取文件存储路径, fileMd5+fileExt组成文件名
     * @author will
     * @date 2023/2/21 16:03
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }
}
