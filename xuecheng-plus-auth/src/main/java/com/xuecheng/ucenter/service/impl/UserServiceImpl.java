package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Mr.M
 * @version 1.0
 * @description 用户信息配置类
 * @date 2022/9/28 18:09
 */
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    /**
     * @param s 传入的是username(账号)
     * @return org.springframework.security.core.userdetails.UserDetails
     * @description 根据账号查询用户信息, 在数据库中获取密码进行比对, 同时使用withUsername内容生成令牌
     * @author Mr.M
     * @date 2022/9/28 18:30
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //从数据库查询信息
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, s));
        if (null == user) {
            //返回空表示用户不存在
            //返回什么的逻辑要看已经给出的框架逻辑，
            //查看源代码DaoAuthenticationProvider得知此时应该返回null
            return null;
        }

        //取出数据库存储的正确密码
        String password_DB = user.getPassword();
        //用户权限,如果不加会报错Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};
        //为了安全在令牌中不放密码(令牌不能存放敏感信息)
        user.setPassword(null);
        //将user对象转json
        String userJson = JSON.toJSONString(user);
        //创建UserDetails对象,权限信息待实现授权功能时再向UserDetail中加入
        UserDetails userDetails = User.withUsername(userJson).password(password_DB).authorities(authorities).build();

        return userDetails;
    }

}