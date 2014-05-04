package smile.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple {@link java.util.concurrent.ThreadFactory} implementation,
 * will return daemon threads ( {@link Thread#isDaemon()} )
 *
 * @author Andre Kullmann
 */
public class DaemonThreadFactory implements ThreadFactory {

    private final AtomicInteger count = new AtomicInteger(1);
    private final String name;

    public DaemonThreadFactory(String name) {
        this.name = name;
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName( name + "-" + count.getAndIncrement() );
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}
