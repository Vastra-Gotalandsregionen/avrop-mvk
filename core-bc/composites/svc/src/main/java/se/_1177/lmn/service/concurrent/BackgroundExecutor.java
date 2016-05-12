package se._1177.lmn.service.concurrent;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Patrik Björk
 */
@Service
public class BackgroundExecutor {

    private static ExecutorService executorService;

    @PreDestroy
    public void shutdown() {
        getExecutor().shutdownNow();
    }

    public synchronized ExecutorService getExecutor() {
        if (executorService != null) {
            return executorService;
        }
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();

        threadFactory.setDaemon(true);
        threadFactory.setThreadGroupName("backgroundTasksGroup");
        threadFactory.setThreadNamePrefix("backgroundTask");

        executorService = Executors.newFixedThreadPool(100, threadFactory);

        return executorService;
    }

    public void submit(Runnable runnable) {
        getExecutor().submit(runnable);
    }

}
