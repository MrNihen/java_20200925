package com.zelin.util;

import com.alibaba.fastjson.JSON;
import com.zeyigou.mapper.TbItemMapper;
import com.zeyigou.pojo.TbItem;
import com.zeyigou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WF on 2020/9/21 10:08
 */
@Component
public class SolrUtils {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
    //1.将tb_item表中的数据导入到索引库中
    public void importData(){
        //1.1)导入sku商品列表
        //1.1.1)定义查询实列
        TbItemExample example = new TbItemExample();
        //1.1.2)定义查询条件
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        //1.1.3)开始条件查询
        List<TbItem> items = itemMapper.selectByExample(example);
        //1.1.4)处理动态域字段
        for (TbItem item : items) {
            //① 得到规格字符串
            String specStr = item.getSpec();
            //② 将规格字符串转换为map
            Map map = JSON.parseObject(specStr, Map.class);
            //③将specMap与item进行绑定
            item.setSpecMap(map);
        }
        //1.2)保存到索引库中
        solrTemplate.saveBeans(items);
        //1.3)提交改变
        solrTemplate.commit();

    }


}
