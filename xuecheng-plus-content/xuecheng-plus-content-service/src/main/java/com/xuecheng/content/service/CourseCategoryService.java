package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程分类操作相关接口
 * @date 2023/2/7 16:54
 */
public interface CourseCategoryService {

    /**
     * @param id 根结点id
     * @return 根节点下边的所有子结点
     * @description 课程分类树形结构查询
     * @author will
     * @date 2023/2/7 16:56
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
