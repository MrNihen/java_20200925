package com.zelin.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;

/**
 * Created by WF on 2020/9/25 9:48
 * 点对点消息的接受方
 */
public class QueueConsumer01 {
    public static void main(String[] args) {
        try {
            //1.得到连接工厂对象
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.134:61616");
            //2.得到连接对象
            Connection connection = connectionFactory.createConnection();
            //3.启动连接
            connection.start();
            //4.得到会话对象
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //5.创建消费者对象
            //5.1)创建目标对象
            Queue dest = new ActiveMQQueue("test-queue");
            //5.2)创建建消费者对象
            MessageConsumer consumer = session.createConsumer(dest);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        //5.2.1)对接受到的消息进行转换
                        TextMessage msg = (TextMessage) message;
                        //5.2.2)得到消息内容
                        String text = msg.getText();
                        //5.2.3)打印
                        System.out.println("收到PTP消息：" + text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.in.read();
            //5.5)关闭资源
            consumer.close();
            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
