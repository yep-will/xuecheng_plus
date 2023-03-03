package com.xuecheng.content.feignclient;

import com.xuecheng.content.feignclient.model.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author will
 * @version 1.0
 * @description 搜索服务远程调用接口
 * @date 2023/3/3 23:49
 */
@FeignClient(value = "search", fallbackFactory = SearchServiceClientFallbackFactory.class)
@RequestMapping("/search")
public interface SearchServiceClient {

    @PostMapping("/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);

}
