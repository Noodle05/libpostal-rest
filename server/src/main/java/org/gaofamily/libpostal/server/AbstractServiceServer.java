package org.gaofamily.libpostal.server;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public abstract class AbstractServiceServer implements ServiceServer {
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            internalStart();
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            internalStop();
        }
    }

    protected abstract void internalStart();

    protected abstract void internalStop();
}
