package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程预览数据模型
 * @date 2023/3/1 9:49
 */
@Data
@ToString
public class CoursePreviewDto {

    /**
     * 课程基本信息,课程营销信息
     */
    CourseBaseInfoDto courseBase;

    /**
     * 课程计划信息
     */
    List<TeachplanDto> teachplans;

    /**
     * 师资信息
     */
    List<CourseTeacher> courseTeachers;

}
