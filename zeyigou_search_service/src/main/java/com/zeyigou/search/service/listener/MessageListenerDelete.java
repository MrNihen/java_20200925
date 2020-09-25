package com.zeyigou.search.service.listener;

import com.zeyigou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * Created by WF on 2020/9/25 11:11
 * 用于监听删除商品
 */
@Component
public class MessageListenerDelete implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        try {
            //1.得到要删除的商品id
            ObjectMessage obj = (ObjectMessage) message;
            //2.得到Long[]对象
            Long[] ids = (Long[]) obj.getObject();
            //3.从索引库中删除商品
            itemSearchService.deleteIndexByGoodsId(ids);
            //4.打印
            System.out.println("从索引库中删除商品.");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
