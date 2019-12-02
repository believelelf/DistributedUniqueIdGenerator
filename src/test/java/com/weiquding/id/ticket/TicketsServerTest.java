package com.weiquding.id.ticket;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * TicketsServer方案测试
 *
 * @author wuby
 * @version V1.0
 * @date 2019/12/2
 */
public class TicketsServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsServerTest.class);

    private Set<Long> ids;

    @Before
    public void setUp() {
        ids = new CopyOnWriteArraySet<>();
    }

    @Test
    public void testNextId() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() + 1, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
        List<Callable<Long>> callAbles = new ArrayList<>(100);
        for (int i = 0; i < 50; i++) {
            callAbles.add(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                   Thread.sleep(50);
                   return TicketsServer.nextId();
                }
            });
        }
        try {
            List<Future<Long>> futures = threadPoolExecutor.invokeAll(callAbles);
            for (Future<Long> future : futures) {
                try {
                    Long id = future.get();
                    if (ids.contains(id)) {
                        LOGGER.info("There are duplicate ids: [{}]", id);
                    }
                    ids.add(id);
                } catch (ExecutionException e) {
                    LOGGER.error("An error occurred while retrieving the id", e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("\nThe size of generated ids :[{}]\nThe generated ids: {}",ids.size(), ids);
    }
}
