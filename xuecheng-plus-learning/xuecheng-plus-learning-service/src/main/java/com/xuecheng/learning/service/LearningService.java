package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @author will
 * @version 1.0
 * @description 在线学习相关接口
 * @date 2023/3/22 14:39
 */
public interface LearningService {

    /**
     * @param userId      用户id
     * @param courseId    课程id
     * @param teachplanId 课程计划id
     * @param mediaId     视频文件id
     * @return com.xuecheng.base.model.RestResponse<java.lang.String>
     * @description 判断学习资格->拥有学习资格远程调用媒资服务查询视频的播放地址
     * @author will
     * @date 2023/3/22 14:39
     */
    RestResponse<String> getVideo(String userId,
                                  Long courseId,
                                  Long teachplanId,
                                  String mediaId);

}
