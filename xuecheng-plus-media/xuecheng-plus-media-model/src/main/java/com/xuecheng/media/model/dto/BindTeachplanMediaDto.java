package com.xuecheng.media.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author will
 * @version 1.0
 * @description 绑定教学计划-媒资参数模型
 * @date 2023/2/25 19:34
 */
@Data
@ApiModel(value = "BindTeachplanMediaDto", description = "教学计划-媒资绑定提交数据")
public class BindTeachplanMediaDto {

    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;


}
