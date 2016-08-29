package com.gft.backend.configs;

import com.gft.backend.utils.MQReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Created by miav on 2016-08-18.
 */
@Configuration
@ComponentScan({ "com.gft.backend" })
@PropertySource("classpath:config.properties")
public class SpringRootConfig {
    public static final String TEST_SPRING_EXCHANGE = "test-spring-exchange";
    final static String TEST_SPRING_MSG = "test-spring-msg";

    @Autowired
    Environment env;

    @Bean(name = "dataSource")
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(env.getProperty("db.driver"));
        driverManagerDataSource.setUrl(env.getProperty("db.url"));
        driverManagerDataSource.setUsername(env.getProperty("db.username"));
        driverManagerDataSource.setPassword(env.getProperty("db.password"));
        return driverManagerDataSource;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(env.getProperty("mq.url"));
        connectionFactory.setUsername(env.getProperty("mq.username"));
        connectionFactory.setPassword(env.getProperty("mq.password"));
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        //The routing key is set to the name of the queue by the broker for the default exchange.
        template.setRoutingKey(TEST_SPRING_MSG);
        //Where we will synchronously receive messages from
        template.setQueue(TEST_SPRING_MSG);
        return template;
    }


    @Bean
    Queue queue() {
        return new Queue(TEST_SPRING_MSG, true, false, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TEST_SPRING_EXCHANGE, true, false);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TEST_SPRING_MSG);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TEST_SPRING_MSG);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MQReceiver receiver() {
        return new MQReceiver();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MQReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

}
