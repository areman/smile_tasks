package smile.tasks;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * This is the base class for queue implementations which follows the pattern: {@link smile.tasks}
 *
 * @see package-smile.tasks.package-info
 *
 * @author Andre Kullmann
 */
public abstract class AbstractTaskQueue<T> {

    private static Logger LOGGER = Logger.getLogger( AbstractTaskQueue.class.getName() );

    private final ExecutorService workerPool;

    private final int capacity;

    private final int threads;

    private final Semaphore semaphore;

    private final String name;

    private final ResultHandler<T> resultHandler;

    /**
     *
     * @param name the queue name
     * @param capacity how many {@link java.util.concurrent.Callable} objects can be queued before the {@link #submit(java.util.concurrent.Callable)} method will blocked ?
     * @param threads how many worker threads are configured in the given {@link java.util.concurrent.ExecutorService} ( see workerPool argurment ) workerPool ?
     * @param workerPool the thread poll which will be used.
     * @param resultHandler will be called for each submitted {@link java.util.concurrent.Callable#call()} result or exception.
     */
    public AbstractTaskQueue(
            String name,
            int capacity,
            int threads,
            ExecutorService workerPool,
            ResultHandler<T> resultHandler ) {

        this.name          = name;
        this.capacity      = capacity;
        this.threads       = threads;
        this.semaphore     = new Semaphore(capacity);
        this.workerPool    = workerPool;
        this.resultHandler = resultHandler;
    }

    /**
     * @return {@link #resultHandler}
     */
    public ResultHandler<T> getResultHandler() {
        return resultHandler;
    }

    /**
     * @return {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@link #workerPool}
     */
    public ExecutorService getWorkerPool() {
        return workerPool;
    }

    /**
     * @return {@link #semaphore}
     */
    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * @return {@link #capacity}
     */
    private int getCapacity() {
        return capacity;
    }

    /**
     * @return {@link #threads}
     */
    private int getThreads() {
        return threads;
    }

    /**
     * @return true if the results can be handled, in most cases these method checks if the current thread is the owner.
     */
    public abstract boolean canHandleResult();

    /**
     * @throws RuntimeException if {@link #canHandleResult()} returns false.
     */
    protected void checkCanHandleResult() {
        if( !canHandleResult() )
            throw new RuntimeException( "illegal thread access." );
    }

    /**
     *
     * @param callable to be execute
     * @return the result of {@link java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)}
     * @throws java.util.concurrent.RejectedExecutionException
     */
    public Future<T> submit(final Callable<T> callable) {

        if (canHandleResult()) {

            if (getWorkerPool().isShutdown()) {

                await();

                throw new RejectedExecutionException();

            } else {

                purge();

                while (!getSemaphore().tryAcquire()) {
                    awaitOneResult();
                }
            }

        } else {

            try {
                getSemaphore().acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        //System.out.println( System.currentTimeMillis() );
        return getWorkerPool().submit(callable);

    }

    /**
     * Will wait until the queue is empty, all submitted {@link java.util.concurrent.Callable} are executed.
     * @return this
     * @throws java.lang.RuntimeException {@link #checkCanHandleResult()} is invoked.
     */
    public AbstractTaskQueue<T> await() {

        checkCanHandleResult();

        //System.out.println(" await " + getSemaphore().availablePermits() + " => " + getCapacity() );

        //int size = getCapacity() - getSemaphore().availablePermits();

        while (!getSemaphore().tryAcquire( getCapacity() )) {
            //System.out.println(" await " + getSemaphore().availablePermits() + " => " + getCapacity() );
            awaitOneResult();
        }

        getSemaphore().release( getCapacity() );

        return this;
    }

    /**
     * Must wait until on result is available. And will invoke the control flow for the received result ( {@link smile.tasks.ResultHandler})
     */
    public abstract void awaitOneResult();

    /**
     * Shutdown this queue and skip all unfinished {@link java.util.concurrent.Callable}.
     * The queue can't be open again.
     */
    public void cancel() {

        List<Runnable> list = getWorkerPool().shutdownNow();

        getSemaphore().release( list.size() );

        // TODO is this a good solution ?
        for( int i=0; i<list.size(); i++ ) {
            Runnable cancelled = list.get(i);

            String msg = "[ " + getClass().getSimpleName() + "." + new Throwable().getStackTrace()[0].getMethodName() + "() " + cancelled.getClass().getSimpleName() + " cancelled. " + (i+1) + " of " + list.size() + " ]";
            System.err.println(msg);
            //getResultHandler().onException( new CancellationException( msg ) );
        }
/*
        // JRuby Threads has a join timeout from 200 ms
        // so we have to ensure that all threads returns
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (canHandleResult()) {
            await();
        }
*/
    }


    /**
     * Execute the {@link smile.tasks.ResultHandler} for all finished {@link java.util.concurrent.Callable} objects.
     * This should only be called on owner thread.
     */
    public abstract void purge();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ class: ").append(getClass().getSimpleName()).append(", ");
        sb.append("name: \"").append(getName()).append("\", ");
        sb.append("capacity: ").append(getCapacity()).append(", ");
        sb.append("workers: ").append(getThreads()).append(", ");
        sb.append("length: ").append(getCapacity() - getSemaphore().availablePermits());
        return sb.append(" ]").toString();
    }

}
