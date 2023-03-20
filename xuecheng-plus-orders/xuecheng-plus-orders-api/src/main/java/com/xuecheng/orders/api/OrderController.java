package com.xuecheng.orders.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author will
 * @version 1.0
 * @description 订单支付相关接口
 * @date 2023/3/20 15:52
 */
@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * @param addOrderDto 商品订单
     * @return com.xuecheng.orders.model.dto.PayRecordDto
     * @description 生成支付二维码
     * @author will
     * @date 2023/3/20 15:53
     */
    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("请登录后继续选课");
        }
        //调用service，完成插入订单信息，插入支付记录，生成支付二维码
        return orderService.createOrder(user.getId(), addOrderDto);
    }


    /**
     * @param payNo        支付交易记录号
     * @param httpResponse http响应
     * @return void
     * @description 扫码下单接口
     * @author will
     * @date 2023/3/20 15:55
     */
    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {

    }

}
