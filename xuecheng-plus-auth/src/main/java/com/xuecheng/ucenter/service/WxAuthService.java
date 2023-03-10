package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * @author will
 * @version 1.0
 * @description 微信扫码接入接口
 * @date 2023/3/10 11:38
 */
public interface WxAuthService {

    /**
     * @param code 授权码
     * @return com.xuecheng.ucenter.model.po.XcUser
     * @description 1.申请令牌，2.携带令牌查询用户信息，3.保存用户信息到数据库
     * @author will
     * @date 2023/3/10 11:39
     */
    XcUser wxAuth(String code);
}
