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

    private class ResultAsListResultHandler<T> implements ResultHandler<T> {

        public int resultCount = 0;

        public int exceptionCount = 0;

        public List<T> results = new ArrayList<T>();

        public List<Exception> exceptions = new ArrayList<Exception>();

        @Override
        public void onResult(T result) {
            resultCount += 1;
            results.add( result );
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
            exceptionCount += 1;
            exceptions.add( e );
        }
    }

    public void testT() {

        ResultAsListResultHandler<String> handler = new ResultAsListResultHandler<String>();

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

    public void testW() {

        ResultAsListResultHandler<String> handler = new ResultAsListResultHandler<String>();

        SimpleTaskQueue<String> queue = new SimpleTaskQueue<String>( 2, 2, handler );

        long start = System.currentTimeMillis();

        for( int i=0; i<10; i++ ) {
            queue.submit( new Callable<String>() {

                @Override
                public String call() throws Exception {
                    Thread.sleep(500);
                    return null;
                }
            } );
        }

        long end = System.currentTimeMillis();

        long time = end - start;

        assertTrue( time + "", time >= 2000 );

        queue.await();

        assertEquals( 0, handler.exceptionCount );
        assertEquals( 10, handler.resultCount );

    }

}
