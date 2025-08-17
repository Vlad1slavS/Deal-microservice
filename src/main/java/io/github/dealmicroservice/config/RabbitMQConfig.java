package io.github.dealmicroservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация RabbitMQ
 */
@Configuration
public class RabbitMQConfig {

    private static final String CONTRACTORS_CONTRACTOR_EXCHANGE = "contractors_contractor_exchange";
    private static final String DEALS_DEAD_EXCHANGE = "deals_dead_exchange";
    private static final String DEAL_CONTRACTOR_DEAD_EXCHANGE = "deal_contractor_dead_exchange";

    public static final String DEALS_CONTRACTOR_QUEUE = "deals_contractor_queue";
    private static final String DEALS_CONTRACTOR_DEAD_QUEUE = "deals_contractor_dead_queue";

    private static final String CONTRACTOR_ROUTING_KEY = "contractor.updated";
    private static final String DEAD_LETTER_ROUTING_KEY = "contractor.dead";

    public static final int DEAD_LETTER_TTL = 300000;

    @Bean
    public DirectExchange contractorsContractorExchange() {
        return new DirectExchange(CONTRACTORS_CONTRACTOR_EXCHANGE);
    }

    @Bean
    public DirectExchange dealsDeadExchange() {
        return new DirectExchange(DEALS_DEAD_EXCHANGE);
    }

    @Bean
    public DirectExchange dealContractorDeadExchange() {
        return new DirectExchange(DEAL_CONTRACTOR_DEAD_EXCHANGE);
    }

    @Bean
    public Queue dealsContractorQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEALS_DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);

        return QueueBuilder
                .durable(DEALS_CONTRACTOR_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue dealsContractorDeadQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", DEAD_LETTER_TTL);
        args.put("x-dead-letter-exchange", DEAL_CONTRACTOR_DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", CONTRACTOR_ROUTING_KEY);

        return QueueBuilder
                .durable(DEALS_CONTRACTOR_DEAD_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding dealsContractorBinding() {
        return BindingBuilder
                .bind(dealsContractorQueue())
                .to(contractorsContractorExchange())
                .with(CONTRACTOR_ROUTING_KEY);
    }

    @Bean
    public Binding dealsContractorDeadBinding() {
        return BindingBuilder
                .bind(dealsContractorDeadQueue())
                .to(dealsDeadExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Binding dealContractorRetryBinding() {
        return BindingBuilder
                .bind(dealsContractorQueue())
                .to(dealContractorDeadExchange())
                .with(CONTRACTOR_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

}

