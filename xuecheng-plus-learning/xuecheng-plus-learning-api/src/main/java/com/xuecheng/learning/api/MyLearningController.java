package com.xuecheng.learning.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author will
 * @version 1.0
 * @description 学习过程管理接口
 * @date 2023/3/22 9:52
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {

    @Autowired
    LearningService learningService;


    /**
     * @param courseId    课程id
     * @param teachplanId 课程计划id
     * @param mediaId     媒资id
     * @return com.xuecheng.base.model.RestResponse<java.lang.String>
     * @description 获取视频播放地址
     * @author will
     * @date 2023/3/22 14:57
     */
    @ApiOperation("获取视频播放地址")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getVideo(@PathVariable("courseId") Long courseId,
                                         @PathVariable("teachplanId") Long teachplanId,
                                         @PathVariable("mediaId") String mediaId) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if (user != null) {
            userId = user.getId();
        }
        //判断学习资格->拥有学习资格远程调用媒资服务查询视频的播放地址
        return learningService.getVideo(userId, courseId, teachplanId, mediaId);
    }

}
