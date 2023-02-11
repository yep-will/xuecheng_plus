package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程计划树型结构dto
 * @date 2023/2/9 15:24
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {

    /**
     * 储存媒资文件名
     */
    private String mediaFileName;

    /**
     * 媒资id
     */
    private Long mediaId;

    /**
     * 课程计划关联的媒资信息
     */
    TeachplanMedia teachplanMedia;

    /**
     * 子目录
     */
    List<TeachplanDto> teachPlanTreeNodes;

}