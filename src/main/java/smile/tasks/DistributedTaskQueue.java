package smile.tasks;


import smile.threads.DaemonThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * It's not really a distributed implementation of {@link smile.tasks.AbstractTaskQueue} but you can easily use it
 * for such a use case. The constructor {@link smile.tasks.DistributedTaskQueue#DistributedTaskQueue(String, int, int, java.util.concurrent.ExecutorService, ResultHandler)}
 * will take an {@link java.util.concurrent.ExecutorService} and if it's distributed so the queue will distribute
 * the submitted {@link java.util.concurrent.Callable} objects.
 *
 * @author Andre Kullmann
 *
 * @param <T> The result type of the submitted {@link java.util.concurrent.Callable} objects.
 */
public class DistributedTaskQueue<T> extends AbstractTaskQueue<T> {

	private final BlockingQueue<TaskResult<T>> resultQueue;

	private final ExecutorService watcherPool;

    private final Thread ownerThread;

    public DistributedTaskQueue(
            int capacity,
            int threads,
            ResultHandler<T> resultHandler) {

        this( "Smile Queue", capacity, threads, resultHandler );
    }

    public DistributedTaskQueue(
            String name,
            int capacity,
            int threads,
            ResultHandler<T> resultHandler ) {

        this(name, capacity, threads,
                Executors.newFixedThreadPool(threads, new DaemonThreadFactory(name + " Worker")),
                resultHandler );
    }

    public DistributedTaskQueue(
            String name,
            int capacity,
            int threads,
            ExecutorService workerPool,
            ResultHandler<T> resultHandler ) {

        super(name, capacity, threads, workerPool, resultHandler );

        this.resultQueue = new LinkedBlockingQueue<TaskResult<T>>();
        this.ownerThread = Thread.currentThread();
        this.watcherPool = Executors.newCachedThreadPool(new DaemonThreadFactory( this.getName() + " Watcher"));
    }


    /**
     * @return {@link #watcherPool}
     */
    private ExecutorService getWatcherPool() {
		return watcherPool;
	}

    /**
     * @return {@link #resultQueue}
     */
    private BlockingQueue<TaskResult<T>> getResultQueue() {
		return resultQueue;
	}

    /**
     * @return {@link #ownerThread}
     */
    private Thread getOwnerThread() {
        return ownerThread;
    }

    /**
     * @return true if {@link Thread#currentThread()} is equal {@link #getOwnerThread()}
     */
    @Override
    public boolean canHandleResult() {
        return Thread.currentThread() == getOwnerThread();
    }


    /**
     * Calls the super method {@link smile.tasks.AbstractTaskQueue#submit(java.util.concurrent.Callable)} and adds
     * some functionality.
     *
     * @param callable to be execute
     * @return the result of super call {@link smile.tasks.AbstractTaskQueue#submit(java.util.concurrent.Callable)}
     */
    @Override
	public Future<T> submit( final Callable<T> callable ) {

        Future<T> future = super.submit( callable );

  		TaskWatcher<T> watcher = new TaskWatcher<T>( future, getResultQueue() );

        getWatcherPool().submit( watcher );

        return future;
	}


    @Override
	public void cancel() {

        super.cancel();

		getWatcherPool().shutdown();

	}

    @Override
    public void purge() {

        checkCanHandleResult();

        for( TaskResult<T> result = getResultQueue().poll(); result != null; result = getResultQueue().poll() ) {

            handleResult(result);
        }
    }

    @Override
    public void awaitOneResult() {

        //checkOwnerThread();

        TaskResult<T> result;
        try {
            result = getResultQueue().take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        handleResult(result);
	}
	

	private void handleResult( TaskResult<T> result ) {

        getSemaphore().release();

        if( result.getValue() != null  ) {
			getResultHandler().onResult(result.getValue());
        }

		if( result.hasException() ) {
			getResultHandler().onException(result.getException());
		}
	}

    /**
     * Simple wrapper for the result of {@link java.util.concurrent.Callable#call()}.
     * Used as {@link #getResultQueue()} entry.
     *
     * @author Andre Kullmann
     * @param <T> the result type
     */
    private static class TaskResult<T> {

        private final T value;

        private final Exception exception;

        public TaskResult(T value) {
            this( value, null );
        }

        public TaskResult(Exception exception) {
            this( null, exception );
        }

        public TaskResult(T value, Exception exception) {
            this.value = value;
            this.exception = exception;
        }

        public T getValue() {
            return value;
        }

        public boolean hasValue() {
            return !hasException();
        }

        public Exception getException() {
            return exception;
        }

        public boolean hasException() {
            return exception != null;
        }

    }

    /**
     * For each submitted {@link java.util.concurrent.Callable} one {@link smile.tasks.DistributedTaskQueue.TaskWatcher} will be
     * started. This watcher will wait until the {@link java.util.concurrent.Future#get()} method of the submitted {@link java.util.concurrent.Callable}
     * will return.
     * It's the only way to get an information if the {@link java.util.concurrent.Future#cancel(boolean)} method is called, without
     * an infinity loop over all futures.
     *
     * @author Andre Kullmann
     * @param <T> the result type
     */
    public class TaskWatcher<T> implements Runnable {

        private final Future<T> future;

        private final BlockingQueue<TaskResult<T>> queue;

        public TaskWatcher(
                final Future<T> future,
                final BlockingQueue<TaskResult<T>> queue) {
            this.future = future;
            this.queue  = queue;
        }

        private BlockingQueue<TaskResult<T>> getQueue() {
            return queue;
        }

        private Future<T> getFuture() {
            return future;
        }

        @Override
        public void run() {

            try {

                T value = getFuture().get();
                TaskResult<T> result = new TaskResult<T>( value );
                submitResult(result);

            } catch( Exception e ) {

                if( e instanceof InterruptedException )
                    Thread.currentThread().interrupt();

                TaskResult<T> result = new TaskResult<T>(e);
                submitResult(result);
            }
        }

        private void submitResult( TaskResult<T> result ) {
            try {
                getQueue().put(result);
            } catch( InterruptedException e ) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
