package com.zelin.test;

import com.zeyigou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WF on 2020/9/21 9:43
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring/applicationContext-*.xml")
public class TestSolrTempalte {

    @Autowired
    private SolrTemplate solrTemplate;
    //1.添加商品到索引库
    @Test
    public void test01(){
        //1.1)定义要添加的商品
        TbItem item = new TbItem();
        item.setId(1L);
        item.setTitle("三星 Note手机");
        item.setBrand("三星");
        item.setPrice(new BigDecimal("29999"));
        item.setSeller("三星专卖南山分部");
        item.setImage("http://www.baidu.com/images/a.jpg");
        item.setGoodsId(1L);
        item.setCategory("手机类");

        //1.2)保存商品
        solrTemplate.saveBean(item);

        //1.3)提交保存
        solrTemplate.commit();
    }

    //2.查询一条数据
    @Test
    public void test02(){
        //2.1)根据id查询单条记录
        TbItem tbItem = solrTemplate.getById(1, TbItem.class);
        //2.2)打印
        System.out.println(tbItem);
    }

    //3.修改记录
    @Test
    public void test03(){
        //3.1)根据id查询单条记录
        TbItem tbItem = solrTemplate.getById(1, TbItem.class);
        tbItem.setTitle("华为 Meta手机");
        tbItem.setBrand("华为");
        tbItem.setSeller("华为南山专卖店");

        //3.2)修改商品
        solrTemplate.saveBean(tbItem);

        //3.3)提交
        solrTemplate.commit();

    }

    //4.删除商品(根据id删除)
    @Test
    public void test04(){
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }

    //5.添加多条记录
    @Test
    public void test05(){
        //1.添加100条记录
        List<TbItem> items = new ArrayList<>();
        for(int i = 1;i <= 100;i++){
            TbItem item = new TbItem();
            item.setId(new Long(i + ""));
            item.setTitle("三星 Note手机" + i);
            item.setBrand("三星");
            item.setPrice(new BigDecimal("29999" + i));
            item.setSeller("三星专卖南山分部" + i);
            item.setImage("http://www.baidu.com/images/a.jpg");
            item.setGoodsId(1L);
            item.setCategory("手机类");
            //添加item对象到集合中
            items.add(item);
        }
        //2.添加多条
        solrTemplate.saveBeans(items);
        //3.提交
        solrTemplate.commit();
    }

    //6. 分页查询
    @Test
    public void test06(){
        //6.1)定义分页查询对象
        SimpleQuery query = new SimpleQuery("*:*");
        //6.2)设置分页查询参数
        query.setOffset(5);     //偏移量（第几条开始）--->（page-1）* pagesize
        query.setRows(10);      //每页记录大小---->pagesize
        //6.3)开始分页查询
        ScoredPage<TbItem> itemScoredPage = solrTemplate.queryForPage(query, TbItem.class);
        //6.4)得到查询结果
        int totalPages = itemScoredPage.getTotalPages();    //总页数
        long total = itemScoredPage.getTotalElements();     //总记录数
        List<TbItem> content = itemScoredPage.getContent(); //每页的记录集合
        //6.5)遍历分页内容
        content.forEach(System.out::println);
    }

    //7.全部删除索引库
    @Test
    public void test07(){
        SimpleQuery query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
    
    //8.根据spu商品id来删除sku列表
    @Test
    public void test08(){
        //8.1)构造查询条件
        SimpleQuery query = new SimpleQuery("item_goodsid:149187842867985");
        //8.2)根据条件进行删除
        solrTemplate.delete(query);
        //8.3)提交删除
        solrTemplate.commit();
    }
}
