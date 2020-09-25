package com.zelin.jms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;

/**
 * Created by WF on 2020/9/25 10:23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-*.xml")
public class SendQueueMessage {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Queue springQueue;
    @Autowired
    private Topic springTopic;
    //1.发送点对点消息
    @Test
    public void testSendQueueMessage(){
        jmsTemplate.send(springQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("正在使用spring发送PTP消息-----1111");
            }
        });
    }
    //2.发送发布/订阅消息
    @Test
    public void testSendTopicMessage(){
        jmsTemplate.send(springTopic, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("正在使用spring发送【发布/订阅】消息!");
            }
        });
    }
}
