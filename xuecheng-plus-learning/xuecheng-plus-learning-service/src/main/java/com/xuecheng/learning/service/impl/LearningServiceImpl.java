package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.mapper.XcLearnRecordMapper;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcLearnRecord;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 在线学习相关接口实现类
 * @date 2023/3/22 14:53
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    XcLearnRecordMapper learnRecordMapper;


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
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //远程调用查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("课程信息不存在");
            return RestResponse.validfail("课程不存在");
        }

        //校验学习资格
        //判断课程计划是否是试学课程
        List<TeachplanDto> teachplans = JSON.parseArray(coursepublish.getTeachplan(), TeachplanDto.class);
        //试学视频直接返回视频地址
        if (isTeachplanPreview(teachplanId, teachplans)) {
            //保存学习记录
            saveLearnRecord(userId, coursepublish, teachplanId);
            //远程调用媒资服务获取视频的播放地址
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        //用户已经登录
        if (StringUtils.isNotEmpty(userId)) {
            //判断是否选课，根据选课情况判断学习资格
            XcCourseTablesDto xcCourseTablesDto = myCourseTablesService.getLearnStatus(userId, courseId);
            //学习资格状态 [{"code":"702001","desc":"正常学习"},
            // {"code":"702002","desc":"没有选课或选课后没有支付"},
            // {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if (learnStatus.equals("702001")) {
                //保存学习记录
                saveLearnRecord(userId, coursepublish, teachplanId);
                //远程调用媒资服务获取视频的播放地址
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702002")) {
                RestResponse.validfail("没有选课或选课后暂未支付");
            } else if (learnStatus.equals("702003")) {
                RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }

        //用户未登录或未选课判断是否收费
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")) {
            //免费可以正常学习，保存学习记录
            saveLearnRecord(userId, coursepublish, teachplanId);
            //远程调用媒资服务获取视频的播放地址
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");
    }


    /**
     * @param userId        用户id
     * @param coursepublish 课程发布信息
     * @param teachplanId   课程计划id
     * @return void
     * @description 保存学习记录
     * @author will
     * @date 2023/3/22 14:51
     */
    public void saveLearnRecord(String userId, CoursePublish coursepublish, Long teachplanId) {
        //登录状态下保存学习记录
        if (StringUtils.isNotEmpty(userId)) {
            //课程id
            Long courseId = coursepublish.getId();
            //找到课程计划对应的名称
            String teachplanName = null;
            List<TeachplanDto> teachplans = JSON.parseArray(coursepublish.getTeachplan(), TeachplanDto.class);
            for (TeachplanDto first : teachplans) {
                if (first.getTeachPlanTreeNodes() != null) {
                    for (TeachplanDto second : first.getTeachPlanTreeNodes()) {
                        if (second.getId().equals(teachplanId)) {
                            teachplanName = second.getPname();
                            break;
                        }
                    }
                }
            }
            //初始化
            learnRecordMapper.initLearnRecord(userId, courseId, teachplanId);
            //更新学习记录
            XcLearnRecord learnRecord_u = new XcLearnRecord();
            learnRecord_u.setCourseName(coursepublish.getName());
            learnRecord_u.setLearnDate(LocalDateTime.now());
            learnRecord_u.setTeachplanName(teachplanName);
            int update = learnRecordMapper.update(learnRecord_u, new LambdaQueryWrapper<XcLearnRecord>().
                    eq(XcLearnRecord::getUserId, userId).
                    eq(XcLearnRecord::getCourseId, courseId).
                    eq(XcLearnRecord::getTeachplanId, teachplanId));
            if (update > 0) {
                log.debug("更新学习记录,user:{},{}", userId, learnRecord_u);
            }
        }
    }


    /**
     * @param teachplanId 课程计划id
     * @param teachplans  课程计划信息
     * @return boolean
     * @description 判断是否为试学课程
     * @author will
     * @date 2023/3/22 14:52
     */
    private boolean isTeachplanPreview(Long teachplanId, List<TeachplanDto> teachplans) {
        for (TeachplanDto first : teachplans) {
            //判断当前课程计划是否有子计划
            if (first.getTeachPlanTreeNodes() != null) {
                for (TeachplanDto second : first.getTeachPlanTreeNodes()) {
                    if (second.getId().equals(teachplanId) && "1".equals(second.getIsPreview())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
