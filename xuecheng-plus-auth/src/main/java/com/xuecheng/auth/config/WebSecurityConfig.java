package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Mr.M
 * @version 1.0
 * @description 安全管理配置
 * @date 2022/9/26 20:53
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //使用自己定义的DaoAuthenticationProviderCustom来代替框架的DaoAuthenticationProvider
    @Autowired
    DaoAuthenticationProviderCustom daoAuthenticationProviderCustom;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProviderCustom);
    }

    /**
     * @return org.springframework.security.authentication.AuthenticationManager
     * @description 配置认证管理bean
     * @author will
     * @date 2023/3/7 16:07
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * @return org.springframework.security.core.userdetails.UserDetailsService
     * @description 配置用户信息服务
     * @author will
     * @date 2023/3/6 23:00
     */
/*
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        //在内存中创建zhangsan，密码123，分配权限p1
        //在内存中创建lisi，密码456，分配权限p2
        manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
        manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
        return manager;
    }
*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        //密码为明文方式
        //return NoOpPasswordEncoder.getInstance();
        return new BCryptPasswordEncoder();
    }

/*
    public static void main(String[] args) {
        String password = "111111";
        PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        for (int i = 0; i < 10; i++) {
            //每个计算出的Hash值都不一样
            String hashPass = bCryptPasswordEncoder.encode(password);
            System.out.println(hashPass);
            //判断密码是否正确
            //虽然每次计算的密码Hash值不一样但是校验是通过的
            boolean f = bCryptPasswordEncoder.matches(password, hashPass);
            System.out.println(f);
        }
    }
*/


    /**
     * @param http 请求路径？
     * @return void
     * @description 配置安全拦截机制
     * @author will
     * @date 2023/3/6 23:00
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                //访问/r开始的请求需要认证通过
                .antMatchers("/r/**").authenticated()
                //其它请求全部放行
                .anyRequest().permitAll()
                .and()
                //登录成功跳转到/login-success
                .formLogin().successForwardUrl("/login-success");

        //退出地址
        http.logout().logoutUrl("/logout");
    }

}
