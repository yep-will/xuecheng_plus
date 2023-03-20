package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

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

}
