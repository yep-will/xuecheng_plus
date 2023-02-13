package com.xuecheng.content.controller;

import com.xuecheng.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 课程营销信息 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping("courseMarket")
public class CourseMarketController {

    @Autowired
    private CourseMarketService courseMarketService;
}
