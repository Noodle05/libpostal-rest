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
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
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
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
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
    public void testLargePacket() throws UnknownHostException, InterruptedException, ExecutionException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
            Map<String, String> requests = new HashMap<>(20);
            for (int i = 0; i < 20; i++) {
                requests.put(Integer.toString(i), "900 Concar Dr, San Mateo, CA 94402 USA");
            }
            Future<Void> future = client.parseAddress(requests, result -> Assert.assertNotNull(result), cause -> {
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
        final Map<String, Map<String, String>> results = new HashMap<>();
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfWorker);
        List<Callable<Map<String, Map<String, String>>>> workers = new ArrayList<>(numberOfWorker);
        try (NettyAddressClient client = new NettyAddressClient(host, port, number, number, null)) {
            for (int i = 0; i < numberOfWorker; i++) {
                workers.add(() -> {
                    Map<String, String> requests = new HashMap<>(4);
                    requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
                    requests.put("2", "1 Market St #300, San Francisco, CA 94105 USA");
                    Future<Void> f1 = client.parseAddress(requests, res -> results.putAll(res), cause -> {
                        Assert.fail("Failed", cause);
                        return null;
                    });

                    requests.clear();
                    requests.put("3", "900 Concar Dr, San Mateo, CA 94402 USA");
                    requests.put("4", "1 Market St #300, San Francisco, CA 94105 USA");
                    Future<Void> f2 = client.parseAddress(requests, res -> results.putAll(res), cause -> {
                        Assert.fail("Failed", cause);
                        return null;
                    });
                    f1.get();
                    f2.get();
                    return null;
                });
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            threadPool.invokeAll(workers);
//            List<Future<Map<String, Map<String, String>>>> futures = threadPool.invokeAll(workers);
//            futures.forEach(future -> {
//                try {
//                    results.putAll(future.get());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            });
            stopWatch.stop();
            long time = stopWatch.getTime();
            System.out.println("Use time: " + time + "ms.");
            Assert.assertFalse(results.isEmpty());
        }
    }
}
