/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict.scheduling;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DictionaryScheduledFuture<V> implements RunnableScheduledFuture<V> {

    @Getter(AccessLevel.PACKAGE)
    private final AtomicBoolean runned = new AtomicBoolean(false);

    @Getter(AccessLevel.PACKAGE)
    private Long id;

    private RunnableScheduledFuture<V> wrapped;

    @Override
    public boolean isPeriodic() {
        return wrapped.isPeriodic();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return wrapped.getDelay(unit);
    }

    @Override
    public int compareTo(Delayed o) {
        return wrapped.compareTo(o);
    }

    @Override
    public void run() {
        synchronized (this) {
            runned.getAndSet(true);
        }
        wrapped.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return wrapped.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return wrapped.isCancelled();
    }

    @Override
    public boolean isDone() {
        return wrapped.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return wrapped.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return wrapped.get(timeout, unit);
    }
}
