package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程计划相关操作接口
 * @date 2023/2/9 16:51
 */
public interface TeachplanService {


    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
     * @description 查询课程相应的课程计划树型结构
     * @author will
     * @date 2023/2/9 16:51
     */
    public List<TeachplanDto> findTeachplanTree(long courseId);


    /**
     * @param dto 课程计划信息
     * @return void
     * @description 保存课程计划(新增/修改)
     * @author will
     * @date 2023/2/9 21:10
     */
    public void saveTeachplan(SaveTeachplanDto dto);
}
