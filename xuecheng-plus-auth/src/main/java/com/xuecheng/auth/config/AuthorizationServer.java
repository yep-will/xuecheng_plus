package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.annotation.Resource;

/**
 * @author Mr.M
 * @version 1.0
 * @description 授权服务器配置
 * @date 2022/9/26 22:25
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {

    @Resource(name = "authorizationServerTokenServicesCustom")
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private AuthenticationManager authenticationManager;

    //客户端详情服务
    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory()// 使用in-memory存储
                // client_id（客户端id，可以是浏览器，也可以是手机）
                .withClient("XcWebApp")
                // 客户端密钥
                .secret("XcWebApp")
                // .secret(new BCryptPasswordEncoder().encode("XcWebApp"))//客户端密钥
                // 资源列表
                .resourceIds("xuecheng-plus")
                // 该client允许的授权类型：授权码模式，密码模式，客户端模式，简化模式，刷新令牌
                .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                // 允许的授权范围
                .scopes("all")
                // false跳转到授权页面
                .autoApprove(false)
                // 客户端接收授权码的重定向地址
                .redirectUris("http://www.xuecheng-plus.com")
        ;
    }

    //令牌端点的访问配置（允许什么样的规则来申请令牌）
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                // 指定认证管理器
                .authenticationManager(authenticationManager)
                // 令牌管理服务
                .tokenServices(authorizationServerTokenServices)
                // 允许提交方式
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    //令牌端点的安全配置
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                // oauth/token_key是公开
                .tokenKeyAccess("permitAll()")
                // oauth/check_token公开
                .checkTokenAccess("permitAll()")
                // 表单认证（申请令牌）
                .allowFormAuthenticationForClients()
        ;
    }

}
