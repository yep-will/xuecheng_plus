package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author will
 * @version 1.0
 * @description FallbackFactory熔断降级策略
 * @date 2023/3/2 21:23
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    //使用FallbackFactory可以获取异常信息
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile upload, String folder, String objectName) {
                //降级方法
                log.error("远程调用媒资管理服务熔断异常：{}", throwable.getMessage());
                return null;
            }
        };
    }
}
