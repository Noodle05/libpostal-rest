package org.gaofamily.libpostal.client;

import org.gaofamily.libpostal.client.netty.NettyAddressClient;
import org.apache.commons.lang3.time.StopWatch;
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
    private static final String host = "localhost";
    private static final int port = 8090;

    @Test(groups = {"integration"})
    public void testParsePositive() throws InterruptedException, UnknownHostException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
            Map<String, String> requests = new HashMap<>(1);
            requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
            Map<String, Map<String, String>> result = client.parseAddress(requests);
            Assert.assertNotNull(result);
            Assert.assertEquals(1, result.size());
        }
    }

    @Test(groups = {"integration"})
    public void testNormalizePositive() throws InterruptedException, UnknownHostException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
            Map<String, String> requests = new HashMap<>(1);
            requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
            Map<String, List<String>> result = client.normalizeAddress(requests);
            Assert.assertNotNull(result);
            Assert.assertEquals(1, result.size());
        }
    }

    @Test(groups = {"integration"})
    public void testLargePacket() throws UnknownHostException, InterruptedException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
            Map<String, String> requests = new HashMap<>(20);
            for (int i = 0; i < 20; i++) {
                requests.put(Integer.toString(i), "900 Concar Dr, San Mateo, CA 94402 USA");
            }
            Map<String, Map<String, String>> result = client.parseAddress(requests);
            Assert.assertNotNull(result);
        }
    }

    @Test(groups = {"integration"})
    public void testMultiThreadParse() throws InterruptedException, UnknownHostException {
        int number = 4;
        int numberOfWorker = 150;
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfWorker);
        List<Callable<Map<String, Map<String, String>>>> workers = new ArrayList<>(numberOfWorker);
        try (NettyAddressClient client = new NettyAddressClient(host, port, number, number, null)) {
            for (int i = 0; i < numberOfWorker; i++) {
                workers.add(() -> {
                    Map<String, Map<String, String>> result = new HashMap<>();
                    Map<String, String> requests = new HashMap<>(4);
                    requests.put("1", "900 Concar Dr, San Mateo, CA 94402 USA");
                    requests.put("2", "1 Market St #300, San Francisco, CA 94105 USA");
                    result.putAll(client.parseAddress(requests));

                    requests.clear();
                    requests.put("3", "900 Concar Dr, San Mateo, CA 94402 USA");
                    requests.put("4", "1 Market St #300, San Francisco, CA 94105 USA");
                    result.putAll(client.parseAddress(requests));
                    return result;
                });
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            List<Future<Map<String, Map<String, String>>>> futures = threadPool.invokeAll(workers);
            Map<String, Map<String, String>> results = new HashMap<>();
            futures.forEach(future -> {
                try {
                    results.putAll(future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
            stopWatch.stop();
            long time = stopWatch.getTime();
            System.out.println("Use time: " + time + "ms.");
            Assert.assertNotNull(results);
        }
    }
}
