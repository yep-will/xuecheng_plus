package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程教师编辑接口
 * @date 2023/2/13 16:47
 */
@Api(value = "教师信息相关接口", tags = "教师信息相关接口")
@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;


    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.po.CourseTeacher>
     * @description 查询相应课程教师列表
     * @author will
     * @date 2023/2/13 17:29
     */
    @ApiOperation("查询课程教师列表接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeacherList(courseId);
    }


    /**
     * @param courseTeacher 课程教师数据
     * @return com.xuecheng.content.model.po.CourseTeacher
     * @description 添加/修改课程教师
     * @author will
     * @date 2023/2/13 20:57
     */
    @ApiOperation("添加/修改课程教师接口")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseTeacherService.saveCourseTeacher(companyId, courseTeacher);
    }


    @ApiOperation("删除教师信息接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, teacherId);
    }

}
