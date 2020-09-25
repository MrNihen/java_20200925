package com.zeyigou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zeyigou.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by WF on 2020/9/21 10:56
 */
@RestController
@RequestMapping("itemsearch")
public class ItemSearchController {

    @Reference(timeout = 50000)
    private ItemSearchService itemSearchService;
    @RequestMapping("search")
    //1.根据请求参数查询商品信息
    public Map search(@RequestBody Map paramMap){
        return itemSearchService.search(paramMap);
    }

    //get
    @RequestMapping("get")
    public void get(HttpSession session){
        Object test = session.getAttribute("test");
        System.out.println("test=" + test);
    }
}
