package me.lucas.kits.orm.activemq.config;

import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

/**
 * Created by zhangxin on 2018/3/28-下午1:36.
 *
 * @author zhangxin
 * @version 1.0
 */
@Configuration
public class Config {
    @Value("activemq.main.defaultDestinationName")
    private String defaultDestinationName;
    @Value("activemq.main.deliveryPersistent")
    private boolean deliveryPersistent;
    @Value("activemq.main.pubSubDomain")
    private boolean pubSubDomain;
    @Value("activemq.main.sessionAcknowledgeMode")
    private int sessionAcknowledgeMode;
    @Value("activemq.main.explicitQosEnabled")
    private boolean explicitQosEnabled;
    @Value("activemq.main.timeToLive")
    private int timeToLive;

    @Bean
    public ActiveMQConnectionFactory jmsFactory(@Value("brokerUrl") String brokerURL) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
        return factory;
    }

    @Bean
    public CachingConnectionFactory connectionFactory(ActiveMQConnectionFactory jmsFactory,
            @Value("sessionCacheSize") Integer sessionCacheSize) {
        CachingConnectionFactory factory = new CachingConnectionFactory(jmsFactory);
        factory.setSessionCacheSize(sessionCacheSize);
        return factory;
    }

    @Bean
    public JmsTemplate queueJmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestinationName(this.defaultDestinationName);
        jmsTemplate.setDeliveryPersistent(this.deliveryPersistent);
        jmsTemplate.setPubSubDomain(this.pubSubDomain);
        jmsTemplate.setSessionAcknowledgeMode(sessionAcknowledgeMode);
        jmsTemplate.setExplicitQosEnabled(this.explicitQosEnabled);
        jmsTemplate.setTimeToLive(this.timeToLive);
        return jmsTemplate;
    }
}
