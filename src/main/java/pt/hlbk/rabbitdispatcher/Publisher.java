package pt.hlbk.rabbitdispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import pt.hlbk.rabbitdispatcher.configuration.RabbitConfig;
import pt.hlbk.rabbitdispatcher.messages.RandomEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

@Service
public class Publisher implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        LOGGER.debug("Getting connection in publisher...");

        publishALotOfMessages(factory);
    }

    private void publishALotOfMessages(ConnectionFactory factory) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            LOGGER.debug("Obtained connection in publisher!");
            channel.queueDeclare(RabbitConfig.queueName, false, false, false, null);
            channel.exchangeDeclare(RabbitConfig.exchangeName, "topic");
            channel.queueBind(RabbitConfig.queueName, RabbitConfig.exchangeName, "");

            IntStream.range(0, 1000000)
                    .forEach(i -> {
                        try {
                            var event = new RandomEvent();
                            event.setOrderId(Integer.toString(random.nextInt(3)));
                            event.setName(UUID.randomUUID().toString());
                            String message = mapper.writeValueAsString(event);
                            channel.basicPublish(RabbitConfig.exchangeName, "", null,  message.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            LOGGER.error("Error publishing", e);
                        }
                    });
        } catch (IOException | TimeoutException e) {
            LOGGER.error("Error publishing", e);
        }
    }
}
