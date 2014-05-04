package smile.tasks.jruby;

import org.jruby.Ruby;
import org.jruby.RubyThread;
import org.jruby.internal.runtime.RubyRunnable;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

/**
 * Wrap a JRuby Block as {@link java.util.concurrent.Callable} so it can be submitted to {@link smile.tasks.AbstractTaskQueue#submit(java.util.concurrent.Callable)}.
 *
 * @author Andre Kullmann
 */
/*package private*/ class JRubyTask implements Callable<IRubyObject> {

    private final Ruby runtime;
    private final IRubyObject[] args;
    private final Block block;

    /*package private*/ JRubyTask(Ruby runtime, IRubyObject[] args, Block block) {
        this.runtime = runtime;
        this.args = args;
        this.block = block;
    }

    /**
     * @return {@link #runtime}
     */
    private Ruby getRuntime() {
        return runtime;
    }

    /**
     * @return {@link #args}
     */
    private IRubyObject[] getArgs() {
        return args;
    }

    /**
     * @return {@link #block}
     */
    private Block getBlock() {
        return block;
    }

    @Override
    public IRubyObject call() throws Exception {

        ThreadContext context = getRuntime().getThreadService().getCurrentContext();

        RubyThread thread = context.getThread();

        RubyRunnable runnable = new RubyRunnable( thread, getArgs(), getBlock() );
        runnable.run();

        throwError( thread );

        return getValue( thread );
    }

    public IRubyObject getValue( RubyThread thread ) throws Exception {
        Field field = RubyThread.class.getDeclaredField("finalResult");
        field.setAccessible(true);
        return (IRubyObject) field.get( thread );
    }

    private void throwError( RubyThread thread ) throws Exception {
        Field field = RubyThread.class.getDeclaredField("exitingException");
        field.setAccessible(true);
        Object e = field.get(thread);

        if( e instanceof Error ) {
            throw (Error) e;
        }

        if( e instanceof Exception ) {
            throw (Exception) e;
        }

        if( e instanceof Throwable) {
            throw new Exception( (Throwable) e );
        }

    }
}
