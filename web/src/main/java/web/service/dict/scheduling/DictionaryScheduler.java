/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict.scheduling;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;
import web.configuration.Settings;
import web.entity.dict.DictionaryParameter;
import web.entity.dict.DictionaryParameter_;
import web.entity.dict.UpdateResult;
import web.entity.dict.UpdateType;
import web.repository.dict.DictionaryParameterRepository;
import web.service.dict.DictionaryParameterService;
import web.service.dict.DictionaryUpdateResult;

@Component
@Log4j2
public class DictionaryScheduler {

    @Autowired
    private DictionaryParameterRepository dictionaryParameterRepository;

    @Autowired
    private DictionaryParameterService dictionaryParameterService;

    @Autowired
    private Settings settings;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private Map<Long, DictionaryScheduledFuture> tasks = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private final AtomicInteger number = new AtomicInteger(1);

            private ThreadGroup threadGroup = new ThreadGroup(DictionaryScheduler.class.getSimpleName());

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(threadGroup, runnable, threadGroup.getName() + "-thread-" + number.getAndIncrement());
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                return thread;
            }
        }) {
            @Override
            protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
                DictionaryCallable dictionaryCallable = (DictionaryCallable) callable;
                DictionaryScheduledFuture<V> scheduledFuture =
                        new DictionaryScheduledFuture<V>(dictionaryCallable.getDictionaryParameter().getId(), task);
                tasks.put(scheduledFuture.getId(), scheduledFuture);
                return scheduledFuture;
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                DictionaryScheduledFuture<DictionaryUpdateResult> scheduledFuture = (DictionaryScheduledFuture<DictionaryUpdateResult>) r;
                try {
                    if (t != null) {
                        log.error(t.getMessage(), t);
                        dictionaryParameterRepository.update(scheduledFuture.getId(), UpdateResult.ERROR, t.getMessage());
                    } else {
                        DictionaryUpdateResult dictionaryUpdateResult = scheduledFuture.get();
                        UpdateResult result = dictionaryUpdateResult != null ? dictionaryUpdateResult.getUpdateResult() : null;
                        if (UpdateResult.SUCCESSFULLY.equals(result)) {
                            dictionaryParameterRepository
                                    .update(scheduledFuture.getId(), dictionaryUpdateResult.getUpdateResult(), dictionaryUpdateResult.getMessage(),
                                            dictionaryUpdateResult.getVersion(), dictionaryUpdateResult.getUpdateDate(), Instant.now());
                        } else {
                            UpdateResult status = result != null ? result : UpdateResult.ERROR;
                            String message = dictionaryUpdateResult != null ? dictionaryUpdateResult.getMessage() : "Update returned no result";
                            dictionaryParameterRepository
                                    .update(scheduledFuture.getId(), status, message);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    dictionaryParameterRepository
                            .update(scheduledFuture.getId(), UpdateResult.ERROR, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                } finally {
                    reschedule(scheduledFuture);
                }
            }
        };
        if (settings.isScheduling()) {
            dictionaryParameterRepository.findAll((root, query, cb) -> cb
                    .and(cb.isTrue(root.get(DictionaryParameter_.enabled)), cb.equal(root.get(DictionaryParameter_.updateType), UpdateType.VARIOUS)))
                                         .forEach(this::schedule);
        }
    }

    private void reschedule(DictionaryScheduledFuture<DictionaryUpdateResult> scheduledFuture) {
        tasks.remove(scheduledFuture.getId());
        DictionaryParameter dictionaryParameter = dictionaryParameterRepository.findOne(scheduledFuture.getId());
        if (dictionaryParameter != null) {
            schedule(dictionaryParameter);
        }
    }

    private void schedule(DictionaryParameter dictionaryParameter) {
        if (dictionaryParameter == null || !settings.isScheduling() || !Boolean.TRUE.equals(dictionaryParameter.getEnabled())) {
            return;
        }
        Instant now = Instant.now();
        Instant instant = new CronSequenceGenerator(dictionaryParameter.getSchedule()).next(Date.from(now)).toInstant();
        scheduledThreadPoolExecutor
                .schedule(new DictionaryCallable(dictionaryParameterService, dictionaryParameter), Duration.between(now, instant).toNanos(),
                          TimeUnit.NANOSECONDS);
    }

    public void submit(DictionaryParameter dictionaryParameter) {
        if (dictionaryParameter == null) {
            return;
        }
        DictionaryScheduledFuture scheduledFuture = tasks.get(dictionaryParameter.getId());
        if (scheduledFuture != null) {
            synchronized (scheduledFuture) {
                if (!scheduledFuture.getRunned().get()) {
                    scheduledFuture.cancel(true);
                }
            }
        }
        scheduledThreadPoolExecutor.submit(new DictionaryCallable(dictionaryParameterService, dictionaryParameter));
    }

    public boolean isRunned(DictionaryParameter dictionaryParameter) {
        DictionaryScheduledFuture scheduledFuture = tasks.get(dictionaryParameter.getId());
        return scheduledFuture != null && scheduledFuture.getRunned().get();
    }

    public boolean isPlanned(DictionaryParameter dictionaryParameter) {
        DictionaryScheduledFuture scheduledFuture = tasks.get(dictionaryParameter.getId());
        return scheduledFuture != null && !scheduledFuture.getRunned().get() && scheduledFuture.getDelay(TimeUnit.NANOSECONDS) > 0;
    }

    public boolean isWaiting(DictionaryParameter dictionaryParameter) {
        DictionaryScheduledFuture scheduledFuture = tasks.get(dictionaryParameter.getId());
        return scheduledFuture != null && !scheduledFuture.getRunned().get() && scheduledFuture.getDelay(TimeUnit.NANOSECONDS) <= 0;
    }

    public Instant getInstant(DictionaryParameter dictionaryParameter) {
        DictionaryScheduledFuture scheduledFuture = tasks.get(dictionaryParameter.getId());
        return scheduledFuture == null ? null : Instant.now().plusNanos(scheduledFuture.getDelay(TimeUnit.NANOSECONDS)).plusMillis(1);
    }

    public void updateSchedule(DictionaryParameter dictionaryParameter) {
        dictionaryParameterRepository.update(dictionaryParameter.getId(), dictionaryParameter.getSchedule(), dictionaryParameter.getEnabled());
        DictionaryScheduledFuture scheduledFuture = tasks.get(dictionaryParameter.getId());
        if (scheduledFuture != null) {
            synchronized (scheduledFuture) {
                if (!scheduledFuture.getRunned().get()) {
                    scheduledFuture.cancel(true);
                    reschedule(scheduledFuture);
                }
            }
        } else {
            schedule(dictionaryParameter);
        }
    }

    @PreDestroy
    private void destroy() {
        scheduledThreadPoolExecutor.shutdown();
    }
}
