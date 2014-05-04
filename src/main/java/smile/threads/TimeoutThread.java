package smile.threads;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Andre Kullmann
 */
public abstract class TimeoutThread {

    public static void start( final long millis, final Runnable runnable ) throws TimeoutException, ExecutionException {
        new TimeoutThread( millis ) {

            @Override
            public void run() {
                runnable.run();
            }

        }.await();
    }

    private final long timeout;

    private final FutureTask<Object> task;

    public TimeoutThread(long timeout) {
        this.timeout = timeout;

        Runnable runInvoker = new Runnable() {
            @Override
            public void run() {
                TimeoutThread.this.run();
            }
        };
        this.task    = new FutureTask<Object>( Executors.callable(runInvoker) );

        Runnable taskRunner = new Runnable() {
            @Override
            public void run() {
                TimeoutThread.this.task.run();
            }
        };

        Thread thread = new Thread(taskRunner);
        thread.start();
    }

    public abstract void run();

    public void await() throws TimeoutException, ExecutionException {
        try {
            this.task.get( this.timeout, TimeUnit.MILLISECONDS );
        } catch( TimeoutException e ) {
            this.task.cancel(true);
            throw e;
        } catch( InterruptedException e ) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
