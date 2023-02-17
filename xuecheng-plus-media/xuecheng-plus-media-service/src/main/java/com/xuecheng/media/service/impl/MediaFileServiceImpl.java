package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.utils.CommonUtils;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
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
     * 普通文件桶，对应mediafiles
     */
    @Value("${minio.bucket.files}")
    private String bucket_files;

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
                    .bucket(bucket_files)
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
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");

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

}
