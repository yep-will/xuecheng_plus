package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程计划 Mapper 接口
 * @date 2023/2/11 16:50
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
     * @description 查询某课程的课程计划，组成树型结构
     * @author will
     * @date 2023/2/9 16:13
     */
    List<TeachplanDto> selectTreeNodes(Long courseId);


}
