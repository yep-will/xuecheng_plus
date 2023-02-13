package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程教师编辑接口
 * @date 2023/2/13 16:49
 */
public interface CourseTeacherService {

    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.po.CourseTeacher>
     * @description 获取课程教师列表
     * @author will
     * @date 2023/2/13 17:21
     */
    List<CourseTeacher> getCourseTeacherList(Long courseId);


    /**
     * @param companyId 机构id
     * @param courseTeacher  课程老师信息
     * @return void
     * @description 新增/修改课程教师
     * @author will
     * @date 2023/2/13 19:54
     */
    CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher);


    /**
     * @param courseId 课程id
     * @param teacherId  教师id
     * @return void
     * @description 删除课程教师
     * @author will
     * @date 2023/2/13 21:11
     */
    void deleteCourseTeacher(Long courseId, Long teacherId);

}
