package com.xuecheng.ucenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author will
 * @version 1.0
 * @description 验证码服务远程接口
 * @date 2023/3/9 18:18
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFactory.class)
@RequestMapping("/checkcode")
public interface CheckCodeClient {

    @PostMapping(value = "/verify")
    Boolean verify(@RequestParam("key") String key,
                   @RequestParam("code") String code);

}
