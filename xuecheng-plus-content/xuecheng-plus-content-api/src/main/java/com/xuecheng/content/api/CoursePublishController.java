package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

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
     * @param courseId  课程id
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

}
