package com.xuecheng.content.api;

import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class FreemarkerController {


    @Autowired
    CoursePublishService coursePublishService;

    @GetMapping("/freemarker")
    public ModelAndView freeMarkerTest(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","小明");
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
