package org.gaofamily.libpostal.client;

import org.gaofamily.libpostal.client.netty.NettyAddressClient;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wei Gao
 * @since 8/17/16
 */
public class NettyAddressClientTest {
    private static final Logger logger = LoggerFactory.getLogger(NettyAddressClientTest.class);

    private static final String host = "localhost";
    private static final int port = 8090;

    @Test(groups = {"integration"})
    public void testParsePositive() throws InterruptedException, UnknownHostException, ExecutionException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5)) {
            Map<String, String> requests = new HashMap<>(1);
            requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
            Future<Void> future = client.parseAddress(requests, result -> {
                logger.info("Get result: {}", result);
                Assert.assertNotNull(result);
                Assert.assertEquals(1, result.size());
            }, cause -> {
                Assert.fail("Failed.", cause);
                return null;
            });
            future.get();
        }
    }

    @Test(groups = {"integration"})
    public void testNormalizePositive() throws InterruptedException, UnknownHostException, ExecutionException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5)) {
            Map<String, String> requests = new HashMap<>(1);
            requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
            Future<Void> future = client.normalizeAddress(requests, result -> {
                Assert.assertNotNull(result);
                Assert.assertEquals(1, result.size());
            }, cause -> {
                Assert.fail("Failed", cause);
                return null;
            });
            future.get();
        }
    }

    @Test(groups = {"integration"})
    public void testLargePack() throws UnknownHostException, InterruptedException, ExecutionException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5)) {
            Map<String, String> requests = new HashMap<>(20);
            for (int i = 0; i < 20; i++) {
                requests.put(Integer.toString(i), "900 Concar Dr, San Mateo, CA 94402 USA");
            }
            Future<Void> future = client.normalizeAddress(requests, result -> Assert.assertNotNull(result), cause -> {
                Assert.fail("Failed", cause);
                return null;
            });
            future.get();
        }
    }

    @Test(groups = {"integration"})
    public void testMultiThreadParse() throws InterruptedException, UnknownHostException {
        int number = 4;
        int numberOfWorker = 150;
        final AtomicInteger counter = new AtomicInteger(0);
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfWorker, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("TestThread-" + counter.incrementAndGet());
            return thread;
        });
        final AtomicInteger total = new AtomicInteger(0);
        List<Callable<Map<String, Map<String, String>>>> workers = new ArrayList<>(numberOfWorker);
        try (NettyAddressClient client = new NettyAddressClient(host, port, number)) {
            for (int i = 0; i < numberOfWorker; i++) {
                workers.add(() -> {
                    try {
                        Map<String, String> requests = new HashMap<>(2);
                        requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
                        requests.put("2", "1 Market St #300, San Francisco, CA 94105 USA");
                        Future<Void> f1 = client.parseAddress(requests, res -> {
                            logger.trace("Get parse result: {}", res);
                            int n = total.addAndGet(res.size());
                            logger.trace("Get {} results so far.", n);
                        }, cause -> {
                            Assert.fail("Failed", cause);
                            return null;
                        });
                        f1.get();
                        logger.trace("This thread done.");
                    } catch (Throwable e) {
                        logger.warn("What?", e);
                    }
                    return null;
                });
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            threadPool.invokeAll(workers);
            stopWatch.stop();
            long time = stopWatch.getTime();
            System.out.println("Use time: " + time + "ms.");
            Assert.assertEquals(total.get(), 300);
        } finally {
            threadPool.shutdown();
        }
        System.out.println("ok");
    }
}
