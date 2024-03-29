package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 自定义UserDetailsService用来对接Spring Security
 * @date 2022/9/28 18:09
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcMenuMapper xcMenuMapper;

    /**
     * Spring容器
     */
    @Autowired
    ApplicationContext applicationContext;

    //不使用：多个AuthService实例无法自动装配，方法中也不使用
    //@Autowired
    //AuthService authService;

    /**
     * @param s 传入的是AuthParamsDto类型的json数据
     * @return org.springframework.security.core.userdetails.UserDetails
     * @description 根据账号查询用户信息, 在数据库中获取密码进行比对
     * @author Mr.M
     * @date 2022/9/28 18:30
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        //认证类型， 有password, wx, 验证码...
        String authType = authParamsDto.getAuthType();

        //根据认证类型从Spring容器取出指定的bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        //调用统一execute方法完成认证
        //XcUserExt xcUserExt = authService.execute(authParamsDto);
        //为方便开发，账号密码认证调用不需要校验验证码的execute方法完成验证，使用强制转换
        XcUserExt xcUserExt = null;
        if (beanName.equals("password_authservice")) {
            PasswordAuthServiceImpl passwordAuthService = (PasswordAuthServiceImpl) authService;
            xcUserExt = passwordAuthService.executeWithoutCheckCode(authParamsDto);
        } else {
            xcUserExt = authService.execute(authParamsDto);
        }

        //认证完成
        //封装xcUserExt用户信息为UserDetails，根据userDetails生成令牌
        UserDetails userDetails = getUserPrincipal(xcUserExt);

        return userDetails;
    }


    /**
     * @param xcUserExt 用户id，主键
     * @return org.springframework.security.core.userdetails.UserDetails 用户信息
     * @description 使用withUsername内容.authorities等信息生成令牌，封装xcUserExt用户信息为UserDetails，
     * @author will
     * @date 2023/3/8 23:01
     */
    public UserDetails getUserPrincipal(XcUserExt xcUserExt) {
        String password = xcUserExt.getPassword();

        //用户权限,如果不加会报错Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};
        //根据用户id查询用户的权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        if (xcMenus.size() > 0) {
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m -> {
                //拿到用户拥有的权限表示符号
                permissions.add(m.getCode());
            });

            //将permissions转成数组
            authorities = permissions.toArray(new String[0]);
        }

        //为了安全在令牌中不放密码(令牌不能存放敏感信息)
        xcUserExt.setPassword(null);
        //将user对象转json
        String userJson = JSON.toJSONString(xcUserExt);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();

        return userDetails;
    }

}
