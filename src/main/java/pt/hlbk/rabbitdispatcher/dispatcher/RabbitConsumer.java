package pt.hlbk.rabbitdispatcher.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.hlbk.rabbitdispatcher.configuration.RabbitConfig;
import pt.hlbk.rabbitdispatcher.messages.RandomEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

public class RabbitConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitConsumer.class);

    private final ConnectionFactory connectionFactory;
    private final ExecutorService executor;

    public RabbitConsumer() {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost"); //rabbitmq
        executor = Executors.newSingleThreadExecutor();
    }

    public void consume() {
        executor.submit(() -> {
            try {
                Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(RabbitConfig.queueName, false, false, false, null);
                channel.exchangeDeclare(RabbitConfig.exchangeName, "topic");
                channel.queueBind(RabbitConfig.queueName, RabbitConfig.exchangeName, "");
                var dispatcher = new RabbitConsumerDispatcher();
                var objectMapper = new ObjectMapper();
                var queue = new LinkedBlockingQueue<RandomEvent>();
                Executors.newSingleThreadExecutor().submit(() -> {
                    while (true) {
                        try {
                            LOGGER.info("Queue has size of {}", queue.size());
                            var deserializedMessage = queue.take();
                            dispatcher.dispatch(deserializedMessage.getOrderId(), deserializedMessage);
                        } catch (InterruptedException e) {
                            LOGGER.error("Error consuming messages.", e);
                        }
                    }
                });

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    LOGGER.debug("Received {} - {}", consumerTag, delivery);
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    var deserializedMessage = objectMapper.readValue(message, RandomEvent.class);
                    try {
                        queue.put(deserializedMessage);
                    } catch (InterruptedException e) {
                        LOGGER.error("Error consuming messages.", e);
                    }
                };

                channel.basicConsume(RabbitConfig.queueName, true, deliverCallback, consumerTag -> {
                });
            } catch (IOException | TimeoutException e) { // | InterruptedException
                LOGGER.error("Error consuming messages.", e);
            }
        });
    }
}
