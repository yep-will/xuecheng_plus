package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 数据字典 前端控制器
 * @date 2023/2/7 15:20
 */
@Slf4j
@Api(value = "课程分类相关接口", tags = "课程分类相关接口")
@RestController
public class CourseCategoryController {

    @Autowired
    CourseCategoryService courseCategoryService;


    /**
     * @return java.util.List<com.xuecheng.content.model.dto.CourseCategoryTreeDto>
     * @description 课程分类查询接口
     * @author will
     * @date 2023/2/11 15:22
     */
    @ApiOperation("课程分类查询接口")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        //传入参数是1不是0,根结点不显示
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDtos;
    }

}
