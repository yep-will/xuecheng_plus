package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
* @description 课程基本信息管理业务接口
* @author will
* @date 2023/2/9 16:51
* @version 1.0
*/
public interface TeachplanService {

    /**
    * @description 查询课程计划树型结构
    * @param courseId 课程id
    * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
    * @author will
    * @date 2023/2/9 16:51
    */
    public List<TeachplanDto> findTeachplanTree(long courseId);

    /**
    * @description 保存课程计划(新增/修改)
    * @param teachplanDto 课程计划信息
    * @return void
    * @author will
    * @date 2023/2/9 21:10
    */
    public void saveTeachplan(SaveTeachplanDto teachplanDto);
}
