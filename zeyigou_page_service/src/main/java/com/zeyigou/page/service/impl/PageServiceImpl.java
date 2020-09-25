package com.zeyigou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.zeyigou.mapper.TbGoodsDescMapper;
import com.zeyigou.mapper.TbGoodsMapper;
import com.zeyigou.mapper.TbItemCatMapper;
import com.zeyigou.mapper.TbItemMapper;
import com.zeyigou.page.service.PageService;
import com.zeyigou.pojo.TbGoods;
import com.zeyigou.pojo.TbGoodsDesc;
import com.zeyigou.pojo.TbItem;
import com.zeyigou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WF on 2020/9/24 10:37
 */
@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;


    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Value("${pageDir}")
    private String pageDir;
    //1.生成静态页面
    @Override
    public void genHtml(Long goodsId) {
        try {
            //1.1)得到模板配置对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //1.2)根据配置对象得到模板对象
            Template template = configuration.getTemplate("item.ftl");
            //1.3)定义数据模型对象
            Map model = new HashMap<>();
            //1.3.1)根据商品id查询商品对象
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            //1.3.2)将商品放到model中
            model.put("goods",tbGoods);
            //1.3.3)查询商品描述对象
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //1.3.4)将商品描述放到model中
            model.put("goodsDesc",tbGoodsDesc);
            //1.3.5)查询一、二、三级分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            //1.3.6)将上面的三及分类放到model中
            model.put("itemCat1",itemCat1);
            model.put("itemCat2",itemCat2);
            model.put("itemCat3",itemCat3);
            //1.3.7)根据商品id查询sku商品列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");    //对默认值字段排序
            List<TbItem> items = itemMapper.selectByExample(example);
            //1.3.8)将数据放到model中
            model.put("items",items);


            //1.4)定义输出流对象
            FileWriter out = new FileWriter(pageDir + goodsId + ".html");
            //1.5)处理模板对象
            template.process(model,out);
            //1.6)关闭流
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
