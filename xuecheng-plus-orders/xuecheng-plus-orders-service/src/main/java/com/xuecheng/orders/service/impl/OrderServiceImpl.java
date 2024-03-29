package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author will
 * @version 1.0
 * @description 订单支付接口实现类
 * @date 2023/3/20 16:01
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Autowired
    OrderServiceImpl currentProxy;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    /**
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return com.xuecheng.orders.model.dto.PayRecordDto 支付交易记录(包括二维码)
     * @description 创建商品订单（插入订单信息，插入支付记录，生成支付二维码）
     * @author will
     * @date 2023/3/20 15:59
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        //1.创建商品订单（插入订单主表&&订单明细表）
        XcOrders orders = saveXcOrders(userId, addOrderDto);

        //2.生成支付记录（插入支付记录表）
        XcPayRecord payRecord = createPayRecord(orders);

        //3.生成二维码
        String qrCode = null;
        //支付二维码的url, 传递本系统支付交易号
        String url = String.format(qrcodeurl, payRecord.getPayNo());
        try {
            //url要可以被模拟器访问到，url为下单接口
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }


    /**
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return com.xuecheng.orders.model.po.XcOrders
     * @description 创建商品订单（插入订单主表&&订单明细表）
     * @author will
     * @date 2023/3/20 16:06
     */
    @Transactional(rollbackFor = Exception.class)
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        //进行幂等性判断：同一个选课记录只能有一个订单
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }

        //插入订单主表
        order = new XcOrders();
        //使用雪花算法生成订单号
        Long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        //"600001"未支付
        order.setStatus("600001");
        order.setUserId(userId);
        //订单类型"60201"
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        //记录选课表的id
        order.setOutBusinessId(addOrderDto.getOutBusinessId());
        int insertOrder = ordersMapper.insert(order);
        if (insertOrder <= 0) {
            XueChengPlusException.cast("添加订单失败");
        }

        //插入订单明细表
        String orderDetailJson = addOrderDto.getOrderDetail();
        //将前端传入的订单明细json串 转成 List
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        //遍历xcOrdersGoodsList插入订单明细表
        xcOrdersGoodsList.forEach(goods -> {
            //设置订单号
            goods.setOrderId(orderId);
            int insertOrdersGoods = ordersGoodsMapper.insert(goods);
            if (insertOrdersGoods <= 0) {
                XueChengPlusException.cast("添加订单明细失败");
            }
        });

        return order;
    }


    /**
     * @param businessId 业务id
     * @return com.xuecheng.orders.model.po.XcOrders
     * @description 根据业务id查询订单，此处业务id就是选课记录表中的主键
     * @author will
     * @date 2023/3/20 19:39
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        //数据库层已经有约束了，所以选择selectOne
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }


    /**
     * @param orders 订单信息
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @description 创建支付交易记录（插入支付记录表）
     * @author will
     * @date 2023/3/20 19:45
     */
    public XcPayRecord createPayRecord(XcOrders orders) {
        Long orderId = orders.getId();
        XcOrders xcOrders = ordersMapper.selectById(orderId);
        //如果订单不存在不能添加支付记录
        if (null == xcOrders) {
            XueChengPlusException.cast("订单不存在");
        }

        //获取订单状态
        String status = xcOrders.getStatus();
        //如果此订单支付结果为成功，不再添加支付记录，避免重复支付
        if ("601002".equals(status)) {
            //支付成功
            XueChengPlusException.cast("此订单已成功");
        }

        //添加支付记录
        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号（传递给支付宝）
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        //商品订单号
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        //"601001"未支付
        payRecord.setStatus("601001");
        payRecord.setUserId(orders.getUserId());

        int insert = payRecordMapper.insert(payRecord);
        if (insert <= 0) {
            XueChengPlusException.cast("插入支付记录失败");
        }

        return payRecord;
    }


    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @description 查询支付交易记录
     * @author will
     * @date 2023/3/20 23:48
     */
    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }


    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.dto.PayRecordDto 支付记录信息
     * @description 1.调用支付宝的接口查询支付结果；2.拿到支付结果更新支付记录表和订单表的支付状态
     * @author will
     * @date 2023/3/21 12:29
     */
    @Override
    public PayRecordDto queryPayResult(String payNo) {
        XcPayRecord payRecord = getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("请重新点击支付获取二维码");
        }
        //支付状态
        String status = payRecord.getStatus();
        //如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }

        //1. 调用支付宝的接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //2. 拿到支付结果更新支付记录表和订单表的支付状态
        currentProxy.saveAliPayStatus(payStatusDto);
        //重新查询支付记录
        payRecord = getPayRecordByPayNo(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);

        return payRecordDto;
    }


    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.dto.PayStatusDto 支付结果信息
     * @description 调用支付宝的接口查询支付结果
     * @author will
     * @date 2023/3/21 12:34
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {
        //========请求支付宝查询支付结果=============
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String resultJson = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                XueChengPlusException.cast("请求支付宝查询支付结果失败");
            }
            //获取支付结果body
            resultJson = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            XueChengPlusException.cast("请求支付宝查询支付结果异常");
        }

        //支付结果body转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //解析/保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(total_amount);

        return payStatusDto;
    }


    /**
     * @param payStatusDto 从支付宝查询到的支付结果信息
     * @return void
     * @description 保存支付宝支付结果
     * @author will
     * @date 2023/3/21 12:35
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付记录号
        String payNO = payStatusDto.getOut_trade_no();
        //从数据库获取支付交易记录
        XcPayRecord payRecordByPayNo = getPayRecordByPayNo(payNO);
        if (payRecordByPayNo == null) {
            XueChengPlusException.cast("无法获取相关支付记录");
        }
        //拿到相关联的订单id
        Long orderId = payRecordByPayNo.getOrderId();
        XcOrders xcOrders = ordersMapper.selectById(orderId);
        if (xcOrders == null) {
            XueChengPlusException.cast("无法获取关联的订单");
        }
        //获取数据库支付状态
        String statusFromDb = payRecordByPayNo.getStatus();
        //如果数据库支付的状态已经是成功了，不再处理了
        if ("601002".equals(statusFromDb)) {
            return;
        }

        //如果支付成功
        //从支付宝查询到的支付结果
        String trade_status = payStatusDto.getTrade_status();
        if (trade_status.equals("TRADE_SUCCESS")) {
            //支付宝返回的信息为支付成功
            //更新支付记录表的状态为支付成功
            payRecordByPayNo.setStatus("601002");
            //支付宝的订单号
            payRecordByPayNo.setOutPayNo(payStatusDto.getTrade_no());
            //第三方支付渠道编号
            payRecordByPayNo.setOutPayChannel("Alipay");
            //支付成功时间
            payRecordByPayNo.setPaySuccessTime(LocalDateTime.now());
            //更新订单表的状态为支付成功
            xcOrders.setStatus("600002");

            //1. 更新xc_pay_record数据库表
            payRecordMapper.updateById(payRecordByPayNo);
            //2. 更新xc_orders数据库表
            ordersMapper.updateById(xcOrders);
        }

        //将消息写到数据库——参数1：支付结果通知类型；参数2: 业务id；参数3:业务类型
        MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", xcOrders.getOutBusinessId(), xcOrders.getOrderType(), null);
        //通知消息
        notifyPayResult(mqMessage);
    }


    /**
     * @param message 引进的自定义的消息sdk
     * @return void
     * @description 发送通知结果
     * @author will
     * @date 2023/3/21 23:12
     */
    @Override
    public void notifyPayResult(MqMessage message) {
        // 1.消息内容，转json
        String msg = JSON.toJSONString(message);
        //创建持久化消息
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加callback 使用correlationData指定回调方法
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        // 3.1.ack，消息成功发送到交换机
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表mq_message中的记录
                        mqMessageService.completed(message.getId());
                    } else {
                        // 3.2.nack，消息发送失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        // 发送消息（指定交换机，路由id，消息内容，指定回调方法）
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }

}
