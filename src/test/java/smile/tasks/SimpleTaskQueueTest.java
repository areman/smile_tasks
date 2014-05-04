package smile.tasks;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Andre Kullmann
 */
public class SimpleTaskQueueTest extends TestCase {

    private Callable<String> stringResultCallable( final String name ) {

        return new Callable<String>() {

            @Override
            public String call() throws Exception {
                return name;
            }

        };
    }

    private Callable<Long> threadIdCallable() {

        return new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return Thread.currentThread().getId();
            }

        };
    }

    private class CountingResultHandler<T> implements ResultHandler<T> {

        public int resultCount = 0;

        public int exceptionCount = 0;

        @Override
        public void onResult(T result) {
            resultCount += 1;
        }

        @Override
        public void onException(Exception e) {
            exceptionCount += 1;
        }
    }

    private class ResultAsListResultHandler<T> extends CountingResultHandler<T> {

        public List<T> results = new ArrayList<T>();

        public List<Exception> exceptions = new ArrayList<Exception>();

        @Override
        public void onResult(T result) {
            super.onResult(result);
            results.add( result );
        }

        @Override
        public void onException(Exception e) {
            super.onException(e);
            exceptions.add( e );
        }
    }

    public void testT() {

        CountingResultHandler<String> handler = new CountingResultHandler<String>();

        SimpleTaskQueue<String> queue = new SimpleTaskQueue<String>( 100, 1, handler );

        for( int i=0; i<100; i++ ) {
            queue.submit( stringResultCallable( "Test " + i ) );
        }

        queue.await();

        assertEquals( 100, handler.resultCount );
        assertEquals( 0, handler.exceptionCount );
    }

    public void testS() {

        ResultAsListResultHandler<Long> handler = new ResultAsListResultHandler<Long>();

        SimpleTaskQueue<Long> queue = new SimpleTaskQueue<Long>( 100, 2, handler );

        for( int i=0; i<100; i++ ) {
            queue.submit( threadIdCallable() );
        }

        queue.await();

        assertEquals( 100, handler.resultCount );
        assertEquals( 0, handler.exceptionCount );

        assertEquals( 2, new HashSet<Long>( handler.results ).size() );
    }

}
