package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程分类树型结点dto
 * @date 2023/2/7 15:38
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    /**
     * 存储叶子结点
     */
    List childrenTreeNodes;
}