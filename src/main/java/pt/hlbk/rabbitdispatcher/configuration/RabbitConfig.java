package pt.hlbk.rabbitdispatcher.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.hlbk.rabbitdispatcher.dispatcher.RabbitConsumer;

@Configuration
public class RabbitConfig {

    public static final String queueName = "test-consumer-queue";
    public static final String exchangeName = "test-consumer-exchange";

//    @Bean
//    Queue queue() {
//        return new Queue(queueName, false);
//    }

    @Bean(initMethod = "consume")
    public RabbitConsumer rabbitConsumer() {
        return new RabbitConsumer();
    }
}
