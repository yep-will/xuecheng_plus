package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * @author will
 * @description 课程分类Mapper接口
 * @return
 * @date 2023/2/7 16:48
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    /**
     * @param id 课程分类标识id
     * @return java.util.List<com.xuecheng.content.model.dto.CourseCategoryTreeDto>
     * @description 根据id寻找当前课程分类的子分类
     * @author will
     * @date 2023/2/11 16:38
     */
    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
