package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class FreemarkerController {


    @GetMapping("/freemarker")
    public ModelAndView freeMarkerTest(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","小明");
        modelAndView.setViewName("test");
        return modelAndView;
    }
}
