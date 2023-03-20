package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

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
        //请求支付宝去下单
        //传入支付记录号，判断支付记录号是否存在
        //如果payNo不存在则提示重新发起支付
        XcPayRecord payRecord = orderService.getPayRecordByPayNo(payNo);
        if (payRecord == null) {
            XueChengPlusException.cast("请重新点击支付获取二维码");
        }
        //判断支付结果是否已支付
        String status = payRecord.getStatus();
        if ("601002".equals(status)) {
            XueChengPlusException.cast("已支付，无需重复支付");
        }

        //构造sdk的客户端对象
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        //创建API对应的request
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        //在公共参数中设置回跳和通知地址
        //alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        //alipayRequest.setNotifyUrl("http://tjxt-user-t.itheima.net/xuecheng/orders/paynotify");
        //填充业务参数
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," +
                " \"total_amount\":\"" + payRecord.getTotalPrice() + "\"," +
                " \"subject\":\"" + payRecord.getOrderName() + "\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");
        String form = "";
        try {
            //请求支付宝下单接口,发起http请求（调用SDK请求支付宝下单）
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        //直接将完整的表单html输出到页面
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

}
