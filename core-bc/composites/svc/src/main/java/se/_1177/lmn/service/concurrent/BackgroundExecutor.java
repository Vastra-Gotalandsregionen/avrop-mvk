package se._1177.lmn.service.concurrent;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class which is similar to an {@link ExecutorService} but exposes only the
 * {@link BackgroundExecutor#submit(java.lang.Runnable)} method. Utilizes a thread pool with fixed size where the
 * threads are created as daemon threads.
 *
 * @author Patrik Björk
 */
@Service
public class BackgroundExecutor {

    private static ExecutorService executorService;

    private final int nThreads = 100;

    @PreDestroy
    public void shutdown() {
        getExecutor().shutdownNow();
    }

    private synchronized ExecutorService getExecutor() {
        if (executorService != null) {
            return executorService;
        }
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();

        threadFactory.setDaemon(true);
        threadFactory.setThreadGroupName("backgroundTasksGroup");
        threadFactory.setThreadNamePrefix("backgroundTask");

        executorService = Executors.newFixedThreadPool(nThreads, threadFactory);

        return executorService;
    }

    public void submit(Runnable runnable) {
        getExecutor().submit(runnable);
    }

}
