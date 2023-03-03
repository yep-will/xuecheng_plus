package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author will
 * @version 1.0
 * @description freemarker测试
 * @date 2023/2/28 16:05
 */
//需要返回一个页面，而不是json数据，所以不使用@RestController
@Controller
public class FreemarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        //设置模型数据
        modelAndView.addObject("name", "小明");
        //设置视图名称，就是模板文件的名称（去掉扩展名）
        modelAndView.setViewName("test");
        return modelAndView;
    }

}
