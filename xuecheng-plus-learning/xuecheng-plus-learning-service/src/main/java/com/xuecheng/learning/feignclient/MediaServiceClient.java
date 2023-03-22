package com.xuecheng.learning.feignclient;

import com.xuecheng.base.model.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author will
 * @version 1.0
 * @description 媒资管理服务远程接口
 * @date 2023/3/22 14:42
 */
@FeignClient(value = "media-api", fallbackFactory = MediaServiceClientFallbackFactory.class)
@RequestMapping("/media")
public interface MediaServiceClient {

    /**
     * @param mediaId 媒资id
     * @return com.xuecheng.base.model.RestResponse<java.lang.String>
     * @description 远程调用获取视频播放地址
     * @author will
     * @date 2023/3/22 14:48
     */
    @GetMapping("/open/preview/{mediaId}")
    RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId);

}
