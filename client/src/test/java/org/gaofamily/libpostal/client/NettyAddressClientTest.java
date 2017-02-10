package org.gaofamily.libpostal.client;

import org.gaofamily.libpostal.client.netty.NettyAddressClient;
import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.model.ParseResult;
import org.apache.commons.lang3.time.StopWatch;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
            List<AddressRequest> requests = new ArrayList<>(1);
            AddressRequest item = new AddressRequest();
            item.setId("1");
            item.setAddress("900 Concar Dr, San Mateo, CA 94402 USA");
            requests.add(item);
            List<ParseResult> result = client.parseAddress(requests);
            Assert.assertNotNull(result);
            Assert.assertEquals(1, result.size());
        }
    }

    @Test(groups = {"integration"})
    public void testNormalizePositive() throws InterruptedException, UnknownHostException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
            List<AddressRequest> requests = new ArrayList<>(1);
            AddressRequest item = new AddressRequest();
            item.setId("1");
            item.setAddress("900 Concar Dr, San Mateo, CA 94402 USA");
            requests.add(item);
            List<NormalizeResult> result = client.normalizeAddress(requests);
            Assert.assertNotNull(result);
            Assert.assertEquals(1, result.size());
        }
    }

    @Test(groups = {"integration"})
    public void testLargePacket() throws UnknownHostException, InterruptedException {
        try (NettyAddressClient client = new NettyAddressClient(host, port, 5, null)) {
            List<AddressRequest> requests = new ArrayList<>(1);
            for (int i = 0; i < 20; i ++) {
                AddressRequest item = new AddressRequest();
                item.setId("1");
                item.setAddress("900 Concar Dr, San Mateo, CA 94402 USA");
                requests.add(item);
            }
            List<ParseResult> result = client.parseAddress(requests);
            Assert.assertNotNull(result);
        }
    }

    @Test(groups = {"integration"})
    public void testMultiThreadParse() throws InterruptedException, UnknownHostException {
        int number = 4;
        int numberOfWorker = 150;
        ExecutorService threadPool = Executors.newFixedThreadPool(numberOfWorker);
        List<Callable<List<ParseResult>>> workers = new ArrayList<>(numberOfWorker);
        try (NettyAddressClient client = new NettyAddressClient(host, port, number, null)) {
            for (int i = 0; i < numberOfWorker; i++) {
                workers.add(() -> {
                    List<ParseResult> result = new ArrayList<>();
                    List<AddressRequest> requests = new ArrayList<>(1);
                    AddressRequest item = new AddressRequest();
                    item.setId("1");
                    item.setAddress("900 Concar Dr, San Mateo, CA 94402 USA");
                    requests.add(item);
                    item = new AddressRequest();
                    item.setId("2");
                    item.setAddress("1 Market St #300, San Francisco, CA 94105 USA");
                    requests.add(item);
                    result.addAll(client.parseAddress(requests));

                    requests.clear();
                    item = new AddressRequest();
                    item.setId("3");
                    item.setAddress("900 Concar Dr, San Mateo, CA 94402 USA");
                    requests.add(item);
                    item = new AddressRequest();
                    item.setId("4");
                    item.setAddress("1 Market St #300, San Francisco, CA 94105 USA");
                    requests.add(item);
                    result.addAll(client.parseAddress(requests));
                    return result;
                });
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            List<Future<List<ParseResult>>> futures = threadPool.invokeAll(workers);
            List<ParseResult> results = new ArrayList<>();
            futures.forEach(future -> {
                try {
                    results.addAll(future.get());
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
