package com.zeyigou.portal.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.zeyigou.content.service.ContentService;
import com.zeyigou.pojo.TbContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by WF on 2020/9/19 10:30
 */
@RestController
public class IndexController {

    @Reference
    private ContentService contentService;
    //1.根据分类id查询广告列表
    @RequestMapping("index")
    public List<TbContent> findCategoryList(Long cid){
        return contentService.findCategoryList(cid);
    }

    @RequestMapping("set")
    public void set(HttpSession session){
        session.setAttribute("test","hello,httpSession!");
    }
}
