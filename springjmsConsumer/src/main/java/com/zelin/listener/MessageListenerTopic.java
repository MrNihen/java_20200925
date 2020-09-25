package com.zelin.listener;

import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Created by WF on 2020/9/25 10:21
 */
@Component
public class MessageListenerTopic implements MessageListener {
    @Override
    public void onMessage(Message message) {
        try {
            //1.转换消息为textMessage消息
            TextMessage msg = (TextMessage) message;
            //2.得到消息内容
            String text = msg.getText();
            //3.打印消息
            System.out.println("text = " + text);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
