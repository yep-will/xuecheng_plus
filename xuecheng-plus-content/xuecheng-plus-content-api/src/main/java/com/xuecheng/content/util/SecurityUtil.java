package com.xuecheng.content.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author will
 * @version 1.0
 * @description 获取当前用户身份工具类
 * @date 2023/3/8 15:29
 */
@Slf4j
public class SecurityUtil {

    public static XcUser getUser() {
        try {
            //从上下文中获取jwt令牌的用户身份信息
            Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principalObj instanceof String) {
                //取出用户身份信息
                String principalJson = principalObj.toString();
                //将json转成对象
                XcUser xcUser = JSON.parseObject(principalJson, XcUser.class);
                return xcUser;
            }
        } catch (Exception e) {
            log.error("解析jwt中的用户身份无法转成XcUser对象:{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    @Data
    public static class XcUser implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;

        private String nickname;

        private String wxUnionid;

        private String companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;

    }

}
