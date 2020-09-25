package com.zeyigou.search.service.listener;

import com.alibaba.fastjson.JSON;
import com.zeyigou.pojo.TbItem;
import com.zeyigou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/**
 * Created by WF on 2020/9/25 10:50
 * 专门处理商品审核进发过来的消息
 */
@Component
public class MessageListenerUpdateStatus implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        try {
            //1.接受到商品审核时发来的消息
            TextMessage msg = (TextMessage) message;
            String text = msg.getText();
            //2.转换为List<TbItem>集合
            List<TbItem> items = JSON.parseArray(text, TbItem.class);
            //3.导入到索引库中
            itemSearchService.importToIndex(items);
            //4.提示
            System.out.println("导入到索引库成功！");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
