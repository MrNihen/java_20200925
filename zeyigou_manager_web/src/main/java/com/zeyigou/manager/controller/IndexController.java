package com.zeyigou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by WF on 2020/9/15 10:09
 */
@RestController
public class IndexController {

    //1. 用户登录
    @RequestMapping("findName")
    public Map<String,String> findName(){
        //1.1)得到登录名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //1.2)定义返回map
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        //1.3)返回
        return map;
    }
}
