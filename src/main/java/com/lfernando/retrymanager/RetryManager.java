package com.lfernando.retrymanager;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class RetryManager<T> {
    private static final long DEF_RETRY_DELAY_MS = 1000;
    private static final long DEF_TIMEOUT_MS = 5000;

    private final Supplier<T> task;
    private final Predicate<T> accept;
    private final Duration timeout;
    private final Duration delay;

    private RetryManager(Supplier<T> task, Predicate<T> accept, Duration timeout, Duration delay) {
        this.task = task;
        this.accept = accept;
        this.timeout = timeout;
        this.delay = delay;
    }

    private RetryManager(Supplier<T> task, Predicate<T> accept) {
        this(task, accept, Duration.ofMillis(DEF_TIMEOUT_MS), Duration.ofMillis(DEF_RETRY_DELAY_MS));
    }

    public static <T> RetryManager<T> supply(Supplier<T> task) {
        return new RetryManager<>(task, null);
    }

    public RetryManager<T> timeout(Duration timeout) {
        return new RetryManager<>(this.task, this.accept, timeout, this.delay);
    }

    public RetryManager<T> delay(Duration delay) {
        return new RetryManager<>(this.task, this.accept, this.timeout, delay);
    }

    public RetryManager<T> accept(Predicate<T> accept) {
        return new RetryManager<>(this.task, accept, this.timeout, this.delay);
    }

    public T get() {
        return retry();
    }

    private T retry() {
        if (null == this.accept) {
            throw new IllegalArgumentException("accept is null");
        }
        if (this.timeout.toNanos() < this.delay.toNanos() ) {
            throw new IllegalArgumentException("timeout is less than retry delay");
        }
        final long deadline = System.nanoTime() + this.timeout.toNanos();
        final long delayMillis = this.delay.toMillis();
        for (int attempt = 0; System.nanoTime() < deadline; attempt++) {
            T result = this.task.get();
            if (this.accept.test(result)) {
                System.out.println("Successfully completed task");
                return result;
            }
            try {
                if (System.nanoTime() < deadline) {
                    TimeUnit.MILLISECONDS.sleep(delayMillis);
                    System.out.println("Slept for retry #" + (attempt + 1));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted");
                return null;
            }
        }
        System.out.println("Unsuccessfully completed task");
        return null;
    }
}
