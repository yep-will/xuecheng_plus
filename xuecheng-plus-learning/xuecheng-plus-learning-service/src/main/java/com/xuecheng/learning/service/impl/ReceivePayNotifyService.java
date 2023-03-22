package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author will
 * @version 1.0
 * @description 接收消息通知类
 * @date 2023/3/22 0:09
 */
@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MyCourseTablesService myCourseTablesService;


    /**
     * @param message amqp消息队列传递的消息
     * @return void
     * @description 监听消息队列接收支付结果通知
     * @author will
     * @date 2023/3/22 0:10
     */
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //解析消息转换成对象
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        //根据消息内容，更新选课记录，向我的课程表插入记录
        //消息类型
        String messageType = mqMessage.getMessageType();
        //订单类型,60201表示购买课程
        String orderType = mqMessage.getBusinessKey2();
        //学习中心服务只要购买课程类的支付订单的结果
        if (PayNotifyConfig.MESSAGE_TYPE.equals(messageType) && "60201".equals(orderType)) {
            //选课记录chooseCourseId
            String chooseCourseId = mqMessage.getBusinessKey1();
            //添加选课（保存选课成功状态）
            boolean b = myCourseTablesService.saveChooseCourseStatus(chooseCourseId);
            if (!b) {
                //添加选课失败，抛出异常，消息重回队列
                XueChengPlusException.cast("保存选课记录状态失败");
            }
        }
    }

}
