package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.media.model.dto.BindTeachplanMediaDto;

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
    List<TeachplanDto> findTeachplanTree(long courseId);


    /**
     * @param dto 课程计划信息
     * @return void
     * @description 保存课程计划(新增 / 修改)
     * @author will
     * @date 2023/2/9 21:10
     */
    void saveTeachplan(SaveTeachplanDto dto);


    /**
     * @param teachplanId 课程计划id
     * @return void
     * @description 删除课程计划(包括大章 / 小节)
     * @author will
     * @date 2023/2/13 10:56
     */
    void deleteTeachplan(Long teachplanId);


    /**
     * @param moveType    移动类型
     * @param teachplanId 课程计划id
     * @return void
     * @description 根据移动类型对课程计划排序
     * @author will
     * @date 2023/2/13 15:40
     */
    void orderByTeachplan(String moveType, Long teachplanId);


    /**
     * @param bindTeachplanMediaDto 绑定参数
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @description 教学计划绑定媒资
     * @author will
     * @date 2023/2/25 19:42
     */
    TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

}
