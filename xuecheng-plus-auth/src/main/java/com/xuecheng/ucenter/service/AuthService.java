package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @author will
 * @version 1.0
 * @description 统一的认证接口
 * @date 2023/3/8 20:35
 */
public interface AuthService {

    /**
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.dto.XcUserExt 用户信息
     * @description 认证方法
     * @author will
     * @date 2023/3/8 20:36
     */
    XcUserExt execute(AuthParamsDto authParamsDto);


    /**
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.dto.XcUserExt 用户信息
     * @description 不校验验证码认证方法——便于开发
     * @author will
     * @date 2023/3/9 19:14
     */
    XcUserExt executeWithoutCheckCode(AuthParamsDto authParamsDto);

}
