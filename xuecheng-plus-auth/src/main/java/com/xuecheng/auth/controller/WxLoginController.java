package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * @author will
 * @version 1.0
 * @description 微信扫码登录接口
 * @date 2023/3/9 21:26
 */
@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        //1.远程调用微信申请令牌; 2.拿到令牌查询用户信息; 3.将用户信息写入本项目数据库
        XcUser xcUser1 = wxAuthService.wxAuth(code);

        //暂时硬编写，便于调试环境
        XcUser xcUser = new XcUser();
        xcUser.setUsername("t1");
        //XcUser xcUser = wxAuthService.wxAuth(code);
        if (xcUser == null) {
            return "redirect:http://www.xuecheng-plus.com/error.html";
        }

        String username = xcUser.getUsername();

        //用户登录完成，直接重定向
        return "redirect:http://www.xuecheng-plus.com/sign.html?username=" + username + "&authType=wx";
    }

}
