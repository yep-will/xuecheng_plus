package com.xuecheng.content.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author will
 * @version 1.0
 * @description 课程发布任务处理
 * @date 2023/3/3 0:34
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;


    /**
     * @return void
     * @description 课程发布任务xxl执行器
     * @author will
     * @date 2023/3/3 0:44
     */
    @XxlJob("CoursePublishJobHandler")//对应yaml配置文件中的appname
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", 5, 60);
    }


    /**
     * @param mqMessage 执行任务内容
     * @return boolean
     * @description 课程发布任务处理
     * @author will
     * @date 2023/3/3 0:36
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        log.debug("开始执行课程发布任务,课程id:{}", mqMessage.getBusinessKey1());
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //课程静态化
        generateAndUploadCourseHtml(mqMessage, courseId);
        //课程缓存
        saveCourseCache(mqMessage, courseId);
        //课程索引
        saveCourseIndex(mqMessage, courseId);

        return true;
    }


    /**
     * @param mqMessage 执行任务内容
     * @param courseId  课程id
     * @return void
     * @description 生成课程静态化页面并上传至文件系统
     * @author will
     * @date 2023/3/3 13:00
     */
    private void generateAndUploadCourseHtml(MqMessage mqMessage, Long courseId) {
        //任务id
        Long id = mqMessage.getId();
        //作消息幂等性处理
        //如果该阶段任务完成了不再处理直接返回
        //第一阶段的状态
        int stageOne = this.getMqMessageService().getStageOne(id);
        if (stageOne > 0) {
            log.debug("当前阶段是静态化课程信息任务已经完成不再处理,任务信息:{}", mqMessage);
            return;
        }

        //调用service将课程信息静态化
        File file = coursePublishService.generateCourseHtml(courseId);
        if (null == file) {
            XueChengPlusException.cast("课程静态化异常");
        }
        //将静态页面上传到minIO
        coursePublishService.uploadCourseHtml(courseId, file);
        //给该阶段任务打上完成标记
        //完成第一阶段的任务
        this.getMqMessageService().completedStageOne(id);
    }


    /**
     * @param mqMessage 执行任务内容
     * @param courseId  课程id
     * @return void
     * @description 向elasticsearch索引保存课程信息
     * @author will
     * @date 2023/3/3 13:01
     */
    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        //任务id
        Long id = mqMessage.getId();
        //作消息幂等性处理：如果该阶段任务完成了不再处理直接返回
        //获取第二阶段的状态
        int stageTwo = this.getMqMessageService().getStageTwo(id);
        if (stageTwo > 0) {
            log.debug("当前阶段是创建课程索引,已经完成不再处理,任务信息:{}", mqMessage);
            return;
        }

        //调用service创建索引
        coursePublishService.saveCourseIndex(courseId);

        //给该阶段任务打上完成标记，完成第二阶段的任务
        this.getMqMessageService().completedStageTwo(id);
    }


    /**
     * @param mqMessage 执行任务内容
     * @param courseId  课程id
     * @return void
     * @description 将课程信息缓存至redis
     * @author will
     * @date 2023/3/3 13:01
     */
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
