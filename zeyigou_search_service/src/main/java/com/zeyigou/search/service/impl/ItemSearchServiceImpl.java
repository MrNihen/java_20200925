package com.zeyigou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.zeyigou.pojo.TbItem;
import com.zeyigou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

/**
 * Created by WF on 2020/9/21 10:58
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    //1.根据请求参数进行查询
    @Override
    public Map search(Map paramMap) {
        //1.0)定义结果map
        Map resultMap = new HashMap();

        //处理关键字中的空格
        paramMap.put("keywords",paramMap.get("keywords").toString().replace(" ",""));
        //1.1)进行高亮查询
        Map highlightMap = highlightQuery(paramMap);
        //1.2)将高亮查询结果放到大map中
        resultMap.putAll(highlightMap);

        //1.3)进行分组查询
        List<String> categoryList = categoryList(paramMap);
        System.out.println("categoryList = " + categoryList);

        //1.4)将分组查询结果放到大map中
        resultMap.put("categoryList",categoryList);

        //1.5)根据分类名称找到模板id，从而在redis中根据这个模板id找到品牌列表及规格列表
        String category = "";
        //1.5.1)看是否后台传入了分类值
        if(paramMap != null ){
            String str = paramMap.get("category") + "";
            if( StringUtils.isNotBlank(str) ){
                category = paramMap.get("category") + "";

            }else if(categoryList != null && categoryList.size() > 0){
                category = categoryList.get(0);
            }
        }
        System.out.println("category = " + category);
        //1.6)从redis中根据分类名称找到模板id，从而找到规格及品牌列表
        Map brandSpecList = brandAndSpecList(category);
        //1.7)将品牌及规格列表放到大map中
        resultMap.putAll(brandSpecList);

        return resultMap;
    }
    //导入sku列表到索引库中
    @Override
    public void importToIndex(List<TbItem> tbItems) {
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
    }
    //从索引库中删除商品
    @Override
    public void deleteIndexByGoodsId(Long[] ids) {
        for (Long id : ids) {
            SimpleQuery query = new SimpleQuery();
            query.addCriteria(new Criteria("item_goodsid").is(id));
            solrTemplate.delete(query);
            solrTemplate.commit();
        }
        System.out.println("从索引库中删除记录成功！");
    }
    //2.进行高亮查询
    private Map highlightQuery(Map paramMap) {
        Map hightLightMap = new HashMap();
        //第一部分：关键字查询
        //2.1)定义高亮查询对象
        HighlightQuery query = new SimpleHighlightQuery();
        //2.2）添加关键字查询
        Criteria criteria = new Criteria("item_keywords");
        //2.3)根据传入的关键字查询参数，构造条件对象
        if(paramMap != null){
            if(StringUtils.isNotBlank(paramMap.get("keywords") + "")){
                //2.3.1)添加查询条件
                criteria.is(paramMap.get("keywords"));
                query.addCriteria(criteria);
            }else{
                return null;
            }


            //第二部分：进行过滤查询
            //2.1)进行分类查询
            if(StringUtils.isNotBlank(paramMap.get("category") + "")){
                //2.1.1)定义分类的过滤查询
                FilterQuery categoryFilterQuery =
                        new SimpleFilterQuery(new Criteria("item_category").is(paramMap.get("category")));
                //2.1.2)将高亮查询与分类过滤查询绑定
                query.addFilterQuery(categoryFilterQuery);
            }
            //2.2)进行品牌查询
            if(StringUtils.isNotBlank(paramMap.get("brand")+"")){
                //2.2.1)定义品牌的过滤查询对象
                FilterQuery brandFilterQuery =
                        new SimpleFilterQuery(new Criteria("item_brand").is(paramMap.get("brand")));
                //2.2.2)将高亮查询与品牌过滤查询绑定
                query.addFilterQuery(brandFilterQuery);
            }
            //2.3)进行规格查询
            String specStr = paramMap.get("spec") + "";
            if(StringUtils.isNotBlank(specStr)) {
                //2.3.1)将规格字符串转换为map对象
                Map specMap = JSON.parseObject(specStr, Map.class);
                //2.3.2)遍历map对象
                for (Object key : specMap.keySet()) {
                    //2.3.3)定义规格过滤查询对象
                    FilterQuery specFileQuery =
                            new SimpleFilterQuery(new Criteria("item_spec_"+key).is(specMap.get(key)));
                    //2.3.4)将规格的过滤查询对象与高亮查询对象绑定
                    query.addFilterQuery(specFileQuery);
                }
            }
            //2.4)进行价格区间查询
            String priceStr = paramMap.get("price")+"";
            if(StringUtils.isNotBlank(priceStr)){
                //2.4.1)拆分价格区间
                String[] split = priceStr.split("-");
                //2.4.2)根据价格区间进行查询
                if(!split[0].equals("0")){  //处理最小值
                    query.addFilterQuery(new SimpleFilterQuery(new Criteria("item_price").greaterThanEqual(split[0])));
                }
                if(!split[1].equals("*")){  //处理最大值
                    query.addFilterQuery(new SimpleFilterQuery(new Criteria("item_price").lessThanEqual(split[1])));
                }
            }
            //2.5)进行排序查询
            //2.5.1)得到排序字段
            String sortField = paramMap.get("sortField")+"";
            //2.5.2)得到代表升序（ASC）还是降序(DESC)的排序关键字
            String sort = paramMap.get("sort")+"";
            //2.5.3)判断排序字段是否存在
            if(StringUtils.isNotBlank(sortField)){
                if("ASC".equals(sort)){     //升序
                    query.addSort(new Sort(Sort.Direction.ASC,"item_" + sortField));
                }else if("DESC".equals(sort)){
                    query.addSort(new Sort(Sort.Direction.DESC,"item_" + sortField));
                }
            }

            //第三部分：分页
            //3.1)得到分页参数
            String pageStr = paramMap.get("page") + "";
            String pageSizeStr = paramMap.get("pageSize") + "";
            int page = 0;
            int pageSize = 0;
            //3.2)处理分页数据
            if(StringUtils.isNotBlank(pageStr)){
                page = new Integer(pageStr);
            }
            if(StringUtils.isNotBlank(pageSizeStr)){
                pageSize = new Integer(pageSizeStr);
            }
            //3.3)开始分页
            query.setOffset((page - 1) * pageSize);     //设置分页的偏移量
            query.setRows(pageSize);                    //设置分页的大小

            //第四部分：高亮查询
            //4.1)设置高亮查询的参数对象
            HighlightOptions options = new HighlightOptions();
            //4.2)向查询参数对象中添加参数
            options.addField("item_title");
            options.setSimplePrefix("<span style='color:red'>");
            options.setSimplePostfix("</span>");
            //4.3)将查询参数与查询对象绑定
            query.setHighlightOptions(options);
            //4.4)得到高亮页对象
            HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
            //4.5)得到高亮入口对象
            List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
            //4.6)遍历入口对象
            for (HighlightEntry<TbItem> highlightEntry : highlighted) {
                //4.6.1)得到原始未经过高亮的对象
                TbItem entity = highlightEntry.getEntity();
                //4.6.2)得到高亮内容,因为所有的高亮对象放入到list集合中，是按照顺序放入的，所以使用了list集合
                List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
                //4.6.3)因为这里只放了一个字段，我们只取第一个值
                if(highlights != null && highlights.size() > 0){
                    //4.6.4)这里得到是第一个高亮字段的值
                    HighlightEntry.Highlight highlight = highlights.get(0);
                    //4.6.5)因为高亮字段可能是多域，所以放在list集合中
                    List<String> snipplets = highlight.getSnipplets();
                    //4.6.6)这里只取第一个值
                    if(snipplets != null && snipplets.size() > 0){
                        //4.6.7)这里取得了放入的title高亮字段值
                        String title = snipplets.get(0);
                        //4.6.8)将原始的实体中的标题字段值改为高亮字段
                        entity.setTitle(title);
                    }

                }
                //4.6.9)将高亮对象放到集合中
            }
            hightLightMap.put("rows",highlightPage.getContent());
            //4.6.10）得到分页的参数
            hightLightMap.put("total",highlightPage.getTotalElements());    //总记录数
            hightLightMap.put("totalPages",highlightPage.getTotalPages());   //总页数
        }
        //4.6.10)返回map
        return hightLightMap;

    }
    //3. 分组查询
    private List<String> categoryList(Map paramMap) {
        //定义分组结果的list集合
         List<String> categoryList = new ArrayList<>();
        //3.1)定义分组查询对象
        SimpleQuery query = new SimpleQuery();
        //3.2)定义关键字查询
        if(paramMap != null){
            Criteria criteria = new Criteria("item_keywords");
            if(StringUtils.isNotBlank(paramMap.get("keywords")+"")){
                criteria.is(paramMap.get("keywords"));
            }
            //3.3)为分组查询对象添加查询条件
            query.addCriteria(criteria);
            //3.4)设置分组查询选项参数对象
            GroupOptions options = new GroupOptions();
            options.addGroupByField("item_category");
            //3.5)设置分给查询参数对象
            query.setGroupOptions(options);
            //3.6)得到分组页对象
            GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
            //3.7)得到分组结果对象
            GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

            //3.8)得到分组条目对象(入口)
            Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
            //3.9)遍历入口对象
            for (GroupEntry<TbItem> groupEntry : groupEntries) {
                //3.9.1)得到分组的值
                String value = groupEntry.getGroupValue();
                //3.9.2)将分组值添加到分组集合中
                categoryList.add(value);
            }

        }
        //3.10）返回分组集合
        return categoryList;
    }

    //4.根据分类名称，找到模板id，从而查找出规格及品牌列表
    private Map brandAndSpecList(String category) {
        //4.1)从redis中根据分类名称找出模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCatList").get(category);

        //4.2)根据模板id找出规格及品牌列表
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        List<Map> brandlist = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        //4.3)定义存放规格及品牌的map
        Map specBrandMap = new HashMap();
        //4.4)将规格及品牌放到上面的map中
        specBrandMap.put("specList",specList);
        specBrandMap.put("brandList",brandlist);
        //4.5)返回
        return specBrandMap;
    }
}
