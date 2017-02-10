package org.gaofamily.libpostal.client.netty;

import org.gaofamily.libpostal.model.internal.BatchAddressResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by wgao on 8/18/16.
 */
class ResponseFuture implements Future<BatchAddressResult> {
    private final Lock lock;
    private final Condition condition;
    private volatile BatchAddressResult result = null;
    private final AtomicBoolean canceled;
    private final AtomicBoolean done;
    private volatile Throwable cause;
    private volatile boolean success;

    ResponseFuture() {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        canceled = new AtomicBoolean(false);
        done = new AtomicBoolean(false);
    }

    void setResult(BatchAddressResult result) {
        assert result != null;
        if (!canceled.get() && done.compareAndSet(false, true)) {
            this.result = result;
            success = true;
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    void setFailure(Throwable cause) {
        if (!canceled.get() && done.compareAndSet(false, true)) {
            this.cause = cause == null ? new InterruptedException() : cause;
            success = false;
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!done.get() && canceled.compareAndSet(false, true)) {
            lock.lock();
            try {
                condition.signal();
            } finally {
                lock.unlock();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        return !done.get() && canceled.get();
    }

    @Override
    public boolean isDone() {
        return done.get() && !canceled.get();
    }

    @Override
    public BatchAddressResult get() throws InterruptedException, ExecutionException {
        if (canceled.get()) {
            throw new InterruptedException("Canceled");
        }
        lock.lock();
        try {
            while (!canceled.get() && !done.get()) {
                condition.await();
            }
            if (done.get()) {
                if (success) {
                    return result;
                } else {
                    throw new ExecutionException(cause);
                }
            } else {
                throw new InterruptedException();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public BatchAddressResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (canceled.get()) {
            throw new InterruptedException("Canceled");
        }
        lock.lock();
        try {
            if (!canceled.get() && !done.get()) {
                condition.await(timeout, unit);
            }
            if (done.get()) {
                if (success) {
                    return result;
                } else {
                    throw new ExecutionException(cause);
                }
            } else if (canceled.get()) {
                throw new InterruptedException();
            } else {
                throw new TimeoutException();
            }
        } finally {
            lock.unlock();
        }
    }
}
