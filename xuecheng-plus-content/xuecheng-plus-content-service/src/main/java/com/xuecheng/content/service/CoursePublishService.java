package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

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

}
