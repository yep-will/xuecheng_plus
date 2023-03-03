package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * @author will
 * @version 1.0
 * @description 课程预览、发布接口
 * @date 2023/3/1 14:44
 */
public interface CoursePublishService {

    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息（包含基本信息，营销信息，课程计划，师资信息）
     * @author will
     * @date 2023/3/1 14:44
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);


    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 提交审核
     * @author will
     * @date 2023/3/1 21:24
     */
    void commitAudit(Long companyId, Long courseId);


    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 课程发布接口
     * @author will
     * @date 2023/3/2 0:34
     */
    void publish(Long companyId, Long courseId);


    /**
     * @param courseId 课程id
     * @return java.io.File
     * @description 课程页面静态化
     * @author will
     * @date 2023/3/3 12:43
     */
    File generateCourseHtml(Long courseId);


    /**
     * @param courseId 课程id
     * @param file     静态话文件
     * @return void
     * @description 上传课程静态化页面
     * @author will
     * @date 2023/3/3 12:43
     */
    void uploadCourseHtml(Long courseId, File file);

}
