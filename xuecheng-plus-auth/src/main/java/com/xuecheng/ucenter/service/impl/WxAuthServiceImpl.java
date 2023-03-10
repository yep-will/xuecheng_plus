package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author will
 * @version 1.0
 * @description 微信扫码认证
 * @date 2023/3/9 21:29
 */
@Slf4j
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    /**
     * 微信appid
     */
    @Value("${weixin.appid}")
    String appid;

    /**
     * 微信app密钥
     */
    @Value("${weixin.secret}")
    String secret;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    XcUserMapper xcUserMapper;


    /**
     * @param code  授权码
     * @return com.xuecheng.ucenter.model.po.XcUser
     * @description TODO
     * @author will
     * @date 2023/3/10 11:58
     */
    @Override
    public XcUser wxAuth(String code) {
        //收到code调用微信接口申请access_token
        Map<String, String> access_token_map = getAccess_token(code);
        if (access_token_map == null) {
            return null;
        }

        //获取用户信息

        //添加用户到数据库
        XcUser xcUser = null;
        return xcUser;
    }


    /**
     * @param authParamsDto 统一认证入口后统一提交的数据
     * @return com.xuecheng.ucenter.model.dto.XcUserExt
     * @description 微信扫码认证，不需要校验验证码，不需要校验密码
     * @author will
     * @date 2023/3/10 11:45
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);

        return xcUserExt;
    }


    /**
     * 携带授权码申请令牌：
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     * <p>
     * 申请访问令牌,响应示例
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    /**
     * @param code 授权码
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @description 携带授权码申请访问令牌
     * @author will
     * @date 2023/3/10 11:47
     */
    private Map<String, String> getAccess_token(String code) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址拼接，最终路径
        String wxUrl = String.format(wxUrl_template, appid, secret, code);
        log.info("调用微信接口申请access_token, url:{}", wxUrl);
        //远程调用此url
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        //获取响应结果（上面的响应示例，json数据）
        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        //将响应结果转成map
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);
        //map返回
        return resultMap;
    }
}
