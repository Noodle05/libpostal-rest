package org.gaofamily.libpostal.client;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public interface AddressClient extends Closeable {
    CompletableFuture<Void> parseAddress(Map<String, String> requests,
                                         Consumer<Map<String, Map<String, String>>> callback,
                                         Function<Throwable, Void> exceptionHandler);

    CompletableFuture<Void> normalizeAddress(Map<String, String> requests, Consumer<Map<String, List<String>>> callback,
                                             Function<Throwable, Void> exceptionHandler);
}
