package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author will
 * @version 1.0
 * @description 账号密码认证
 * @date 2023/3/8 22:43
 */
@Slf4j
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    /**
     * @param authParamsDto 统一认证入口后统一提交的数据
     * @return com.xuecheng.ucenter.model.dto.XcUserExt
     * @description 实现账号和密码认证
     * @author will
     * @date 2023/3/9 18:37
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //获取输入的验证码
        String checkcode = authParamsDto.getCheckcode();
        //验证码对应的key
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (StringUtils.isBlank(checkcodekey) || StringUtils.isBlank(checkcode)) {
            throw new RuntimeException("验证码为空");
        }

        //远程调用验证码服务接口去校验验证码
        Boolean result = checkCodeClient.verify(checkcodekey, checkcode);
        if (result == null || !result) {
            throw new RuntimeException("验证码输入错误");
        }

        //账号
        String username = authParamsDto.getUsername();
        //根据username从数据库查询用户信息
        XcUser xcUser = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            //账号不存在
            throw new RuntimeException("账号不存在");
        }
        //比对密码
        String passwordDB = xcUser.getPassword();
        String passwordInput = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordInput, passwordDB);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }


    /**
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.dto.XcUserExt 用户信息
     * @description 不校验验证码认证方法——便于开发
     * @author will
     * @date 2023/3/9 19:14
     */
    @Override
    public XcUserExt executeWithoutCheckCode(AuthParamsDto authParamsDto) {

        //账号
        String username = authParamsDto.getUsername();
        //根据username从数据库查询用户信息
        XcUser xcUser = userMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            //账号不存在
            throw new RuntimeException("账号不存在");
        }
        //比对密码
        String passwordDB = xcUser.getPassword();
        String passwordInput = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordInput, passwordDB);
        if (!matches) {
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
