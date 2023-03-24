package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程预览，发布
 * @date 2023/3/1 9:06
 */
@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;


    /**
     * @param courseId 课程id
     * @return org.springframework.web.servlet.ModelAndView
     * @description 获取课程预览信息模板引擎需要的模型数据
     * @author will
     * @date 2023/3/1 15:39
     */
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息（包含基本信息，营销信息，课程计划，师资信息未做）
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }


    /**
     * @param courseId 课程id
     * @return void
     * @description 提交审核
     * @author will
     * @date 2023/3/1 21:17
     */
    @ResponseBody //将java对象转为json格式的数据
    @ApiOperation("提交课程审核")
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }


    /**
     * @param courseId 课程id
     * @return void
     * @description 课程发布
     * @author will
     * @date 2023/3/3 16:04
     */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }


    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.po.CoursePublish
     * @description 根据课程id查询课程发布信息
     * @author will
     * @date 2023/3/12 22:16
     */
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")//把/r开头的请求路径作为服务内部去调用的接口（内部之间调用不用传令牌）
    public CoursePublish queryCoursePublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }


    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览数据
     * @author will
     * @date 2023/3/22 9:39
     */
    @ApiOperation("获取课程预览数据")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
        //查询课程发布表
        //CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        if (coursePublish == null) {
            return new CoursePreviewDto();
        }

        //1.获取课程基本信息
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);
        //2.获取课程计划
        String teachplanJson = coursePublish.getTeachplan();
        List<TeachplanDto> teachplanDtos = JSON.parseArray(teachplanJson, TeachplanDto.class);
        //3.获取师资信息
        String teacherJson = coursePublish.getTeachers();
        List<CourseTeacher> courseTeachers = JSON.parseArray(teacherJson, CourseTeacher.class);

        //封装数据
        CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
        coursePreviewInfo.setCourseBase(courseBase);
        coursePreviewInfo.setTeachplans(teachplanDtos);
        coursePreviewInfo.setCourseTeachers(courseTeachers);

        return coursePreviewInfo;
    }

}
