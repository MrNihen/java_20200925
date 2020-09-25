package com.zeyigou.search.service;

import com.zeyigou.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * Created by WF on 2020/9/21 10:58
 */
public interface ItemSearchService {
    Map search(Map paramMap);

    void importToIndex(List<TbItem> tbItems);

    void deleteIndexByGoodsId(Long[] ids);
}
