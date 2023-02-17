package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类接口
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param companyId           机构id
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author will
     * @date 2023/2/16 21:54
     */
    public PageResult<MediaFiles> queryMediaFiles(Long companyId,
                                                  PageParams pageParams,
                                                  QueryMediaParamsDto queryMediaParamsDto);


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
    public UploadFileResultDto uploadFile(Long companyId,
                                          UploadFileParamsDto uploadFileParamsDto,
                                          byte[] bytes,
                                          String folder,
                                          String objectName);


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
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileId,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket,
                                        String objectName);
}
