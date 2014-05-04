package smile.tasks;

import smile.threads.DaemonThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This implementation of {@link smile.tasks.AbstractTaskQueue} should be used if an single jvm is used ( no distribution over more running JVMs )
 *
 * @author Andre Kullmann
 */
public class SimpleTaskQueue<T> extends AbstractTaskQueue<T> {

    public static class QueueIsClosedException extends RuntimeException {

    }

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final BlockingQueue<Future<T>> resultQueue;

    private final Thread ownerThread;

    public SimpleTaskQueue(
            int capacity,
            int threads,
            ResultHandler<T> resultHandler) {
        this("Smile Queue", capacity, threads, resultHandler);
    }

    public SimpleTaskQueue(
            String name,
            int capacity,
            int threads,
            ResultHandler<T> resultHandler) {

        super(name,
                capacity,
                threads,
                new TaskQueueExecutor(threads, new DaemonThreadFactory(name)),
                resultHandler);

        this.ownerThread = Thread.currentThread();

        this.resultQueue = wildCast(getWorkerPool().getResultQueue());
    }

    @SuppressWarnings("unchecked")
    private <T> T wildCast(Object object) {
        return (T) object;
    }

    @Override
    public TaskQueueExecutor getWorkerPool() {
        return (TaskQueueExecutor) super.getWorkerPool();
    }

    /**
     * Open this queue. Now {@link java.util.concurrent.Callable} can be submitted.
     *
     * @return this
     */
    public AbstractTaskQueue<T> open() {
        this.open.set(true);
        return this;
    }

    /**
     * Close this queue. NO {@link java.util.concurrent.Callable} can be submitted.
     *
     * @return this
     */
    public AbstractTaskQueue<T> close() {
        this.open.set(false);
        return this;
    }

    public boolean isOpen() {
        return this.open.get();
    }

    @Override
    public void cancel() {

        close();

        super.cancel();
    }


    /**
     *
     * @param callable to be execute
     * @return this
     * @see AbstractTaskQueue#submit(java.util.concurrent.Callable)
     * @throws QueueIsClosedException
     */
    @Override
    public Future<T> submit(Callable<T> callable) {

        if (!isOpen()) {
            throw new QueueIsClosedException();
        }

        return super.submit(callable);
    }

    /**
     *
     * @see AbstractTaskQueue#await()
     * @return this
     */
    @Override
    public AbstractTaskQueue<T> await() {

        close();

        return super.await();
    }

    /**
     * @return {@link #resultQueue}
     */
    private BlockingQueue<Future<T>> getResultQueue() {
        return resultQueue;
    }

    /**
     * @return {@link #ownerThread}
     */
    private Thread getOwnerThread() {
        return ownerThread;
    }

    /**
     * @return {@link #isOwnerThread()}
     */
    @Override
    public boolean canHandleResult() {
        return isOwnerThread();
    }

    /**
     * @return true if {@link Thread#currentThread()} is equals {@link #getOwnerThread()}
     */
    private boolean isOwnerThread() {
        return Thread.currentThread() == getOwnerThread();
    }

    @Override
    public void purge() {

        checkCanHandleResult();

        for (Future<T> future = getResultQueue().poll(); future != null; future = getResultQueue().poll()) {

            handleFuture(future);
        }
    }

    @Override
    public void awaitOneResult() {

        //checkOwnerThread();

        Future<T> future;
        try {
            future = getResultQueue().take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        handleFuture(future);
    }

    private void handleFuture(Future<T> future) {

        getSemaphore().release();

        try {
            T result = future.get();
            getResultHandler().onResult(result);
        } catch (Exception e) {
            getResultHandler().onException(e);
        }
    }

    /**
     * @author Andre Kullmann
     */
    private static class TaskQueueFuture<V> extends FutureTask<V> {

        private final BlockingQueue<Future> resultQueue;

        public TaskQueueFuture(Callable<V> callable, BlockingQueue<Future> resultQueue) {
            super(callable);
            this.resultQueue = resultQueue;
        }

        public TaskQueueFuture(Runnable runnable, V result, BlockingQueue<Future> resultQueue) {
            super(runnable, result);
            this.resultQueue = resultQueue;
        }

        public BlockingQueue<Future> getResultQueue() {
            return resultQueue;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void done() {
            try {
                super.done();
            } finally {
                try {
                    BlockingQueue queue = getResultQueue();
                    queue.put(this);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @author Andre Kullmann
     */
    private static class TaskQueueExecutor extends ThreadPoolExecutor {

        private final BlockingQueue<Future> resultQueue;

        public TaskQueueExecutor(int threads, ThreadFactory threadFactory) {

            super(threads, threads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    threadFactory);

            this.resultQueue = new LinkedBlockingQueue<Future>();
        }

        public BlockingQueue<Future> getResultQueue() {
            return resultQueue;
        }

        @Override
        protected <V> RunnableFuture<V> newTaskFor(Runnable runnable, V value) {
            return new TaskQueueFuture<V>(runnable, value, getResultQueue());
        }

        @Override
        protected <V> RunnableFuture<V> newTaskFor(Callable<V> callable) {
            return new TaskQueueFuture<V>(callable, getResultQueue());
        }
    }
}

