package smile.tasks.jruby;

import org.jruby.Ruby;
import org.jruby.RubyThread;
import org.jruby.internal.runtime.RubyRunnable;
import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

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

        return thread.value();
    }
}
