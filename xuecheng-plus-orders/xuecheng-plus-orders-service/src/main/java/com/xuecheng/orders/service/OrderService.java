package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * @author will
 * @version 1.0
 * @description 订单支付接口
 * @date 2023/3/20 15:58
 */
public interface OrderService {

    /**
     * @param userId      用户id
     * @param addOrderDto 订单信息
     * @return com.xuecheng.orders.model.dto.PayRecordDto 支付交易记录(包括二维码)
     * @description 创建商品订单（插入订单信息，插入支付记录，生成支付二维码）
     * @author will
     * @date 2023/3/20 15:59
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);


    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @description 查询支付交易记录
     * @author will
     * @date 2023/3/20 23:48
     */
    XcPayRecord getPayRecordByPayNo(String payNo);


    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.dto.PayRecordDto 支付记录信息
     * @description 1.调用支付宝的接口查询支付结果；2.拿到支付结果更新支付记录表和订单表的支付状态
     * @author will
     * @date 2023/3/21 12:29
     */
    PayRecordDto queryPayResult(String payNo);

}
