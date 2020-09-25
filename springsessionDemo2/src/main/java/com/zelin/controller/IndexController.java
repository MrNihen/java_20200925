package com.zelin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("")
public class IndexController {

    @RequestMapping("set")
    @ResponseBody
    public String login(HttpSession httpsession){
        httpsession.setAttribute("user", "张三");
        return "login";
    }

    @RequestMapping("get")
    @ResponseBody
    public String check(HttpSession httpsession){
        String userId = (String)httpsession.getAttribute("user");
        System.out.println("userID:" + userId);
        return userId ;
    }
}