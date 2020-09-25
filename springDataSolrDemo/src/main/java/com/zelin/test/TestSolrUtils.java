package com.zelin.test;

import com.zelin.util.SolrUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by WF on 2020/9/21 10:24
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring/applicationContext-*.xml")
public class TestSolrUtils {
    @Autowired
    private SolrUtils solrUtils;
    @Test
    public void testImportData(){
        solrUtils.importData();
    }
}
