package pt.hlbk.rabbitdispatcher.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class RabbitConsumerDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitConsumerDispatcher.class);

    private final int numOfPartitions = 3;

    private final List<Worker> workers;
    private final Executor executor;
    private final List<LinkedBlockingQueue<Object>> queueList;

    public RabbitConsumerDispatcher() {
        queueList = new ArrayList<>();
        IntStream.range(0, numOfPartitions)
                .boxed()
                .forEach(i -> {
                    queueList.add(new LinkedBlockingQueue<>(100));
                });

        executor = Executors.newFixedThreadPool(numOfPartitions);
        workers = new ArrayList<>();

        IntStream.range(0, numOfPartitions)
                .boxed()
                .map(i -> new Worker(i, queueList.get(i)))
                .forEach(worker -> {
                    workers.add(worker);
                    executor.execute(worker);
                });
    }

    public void dispatch(String id, Object message) {
        try {
            queueList.get(id.hashCode() % numOfPartitions).put(message);
        } catch (InterruptedException e) {
            LOGGER.error("Error putting message", e);
        }
    }

    private static class Worker implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

        private final int id;
        private final LinkedBlockingQueue<Object> queue;

        public Worker(int id, LinkedBlockingQueue<Object> queue) {
            this.id = id;
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    var message = queue.take();
                    Thread.sleep(100); // simulates some work!
                    LOGGER.info("Worker#{} working on {}", id, message);
                } catch (InterruptedException e) {
                    LOGGER.error("Error while waiting", e);
                }
            }
        }
    }
}
