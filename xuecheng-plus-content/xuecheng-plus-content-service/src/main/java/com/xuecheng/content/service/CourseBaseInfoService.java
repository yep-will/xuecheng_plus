package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @author will
 * @version 1.0
 * @description 课程基本信息管理业务接口
 * @date 2023/2/6 21:42
 */
public interface CourseBaseInfoService {

    /**
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 查询条件
     * @param companyId            机构id
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @description 课程查询接口
     * @author will
     * @date 2023/2/6 21:43
     */
    PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * @param companyId    教学机构id
     * @param addCourseDto 课程基本信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto 课程信息包括基本信息、营销信息
     * @description 添加课程基本信息接口
     * @author will
     * @date 2023/2/7 21:40
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @description 根据课程id查询课程的基本和营销信息
     * @author will
     * @date 2023/2/9 10:47
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * @param companyId 机构id 要校验本机构只能修改本机构的课程
     * @param dto       修改课程信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @description 修改课程信息
     * @author will
     * @date 2023/2/9 10:59
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);


    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 删除课程(包含基本信息 、 营销信息 、 课程计划 、 课程教师)
     * @author will
     * @date 2023/2/13 22:17
     */
    void delectCourse(Long companyId, Long courseId);
}
