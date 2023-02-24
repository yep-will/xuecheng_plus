package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author will
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2023/2/16 21:37
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    @Autowired
    MediaFileService mediaFileService;


    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询参数
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资列表查询接口
     * @author will
     * @date 2023/2/17 12:48
     */
    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);

    }


    /**
     * @param filedata   文件信息
     * @param folder     目录
     * @param objectName 存储在minIO上的完整路径名称
     * @return com.xuecheng.media.model.dto.UploadFileResultDto
     * @description 上传文件
     * @author will
     * @date 2023/2/17 12:48
     */
    @ApiOperation("上传文件")
    //文档没写是POST还是GET，所以不要乱写请求方式，RequestMapping同时支持GET和POST；consumes参数接收的类型
    @RequestMapping(value = "/upload/coursefile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata,
                                      //required = false：可以不传递该参数
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName) {

        Long companyId = 1232141425L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        String contentType = filedata.getContentType();
        uploadFileParamsDto.setContentType(contentType);
        //设置文件大小
        uploadFileParamsDto.setFileSize(filedata.getSize());
        if (contentType.indexOf("image") >= 0) {
            //是图片
            uploadFileParamsDto.setFileType("001001");
        } else {
            //其它类型文件
            uploadFileParamsDto.setFileType("001003");
        }
        //文件名称
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        UploadFileResultDto uploadFileResultDto = null;
        try {
            uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamsDto, filedata.getBytes(), folder, objectName);
        } catch (Exception e) {
            XueChengPlusException.cast("上传文件过程中出错");
        }

        return uploadFileResultDto;
    }


    /**
     * @param mediaId 文件id
     * @return com.xuecheng.base.model.RestResponse<java.lang.String>
     * @description 预览文件
     * @author will
     * @date 2023/2/21 23:40
     */
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        //调用service查询文件的url
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        return RestResponse.success(mediaFiles.getUrl());
    }

}
