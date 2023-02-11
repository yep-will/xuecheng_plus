package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程计划编辑接口
 * @date 2023/2/9 15:28
 */
@Api(value = "课程计划管理相关接口", tags = "课程计划管理相关的接口")
@Slf4j
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;


    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
     * @description 查询课程计划树形结构接口
     * @author will
     * @date 2023/2/11 15:25
     */
    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", name = "课程基础Id值", required = true, dataType = "Long", paramType = "path")
    @GetMapping("teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }


    /**
     * @param teachplan 课程计划
     * @return void
     * @description 创建或修改课程计划
     * @author will
     * @date 2023/2/11 15:28
     */
    @ApiOperation("创建或修改课程计划")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

}
