package smile.tasks.jruby;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyClass;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.Block;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import smile.core.friend.jruby.JRubyFriend;
import smile.core.friend.jruby.JRubyHashFriend;
import smile.tasks.AbstractTaskQueue;
import smile.tasks.SimpleTaskQueue;
import smile.tasks.ResultHandler;

import static smile.core.friend.jruby.JRubyFriend.friend;

/**
 * JRuby Wrapper for {@link smile.tasks.AbstractTaskQueue}
 *
 * @author Andre Kullmann
 */
@JRubyClass(name = "Smile::Tasks::SimpleTaskQueue")
public class JRubySimpleTaskQueue extends RubyObject {

    private static final ObjectAllocator ALLOCATOR = new ObjectAllocator() {

        public IRubyObject allocate(Ruby runtime, RubyClass klass) {

            return new JRubySimpleTaskQueue(runtime, klass);
        }
    };

    public static RubyClass define(Ruby runtime) {

        try {
            RubyClass result = friend(runtime).clazz(JRubySimpleTaskQueue.class).define(ALLOCATOR);
            //System.out.println( result.getName() );
            return result;
        } catch( RuntimeException e ) {
            e.printStackTrace();
            throw e;
        }

    }

    private Block onErrorBlock;

    private Block onResultBlock;

    private AbstractTaskQueue<IRubyObject> taskQueue;

    private final JRubyFriend friend;

    private JRubySimpleTaskQueue(Ruby runtime, RubyClass klass) {
        super(runtime, klass);
        this.friend = friend( runtime );
    }

    /**
     *
     * @param context current jruby thread context
     * @param args first parameter should be an option hash
     *             :capacity - capacity of the queue
     *             :threads - count of worker threads
     * @return this
     */
    @JRubyMethod(name = "initialize", optional = 1)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) {

        int cpuCount  = Runtime.getRuntime().availableProcessors();
        JRubyHashFriend map = friend.array( args ).mapFriend(0);
        int capacity  = map.intValue( "capacity", cpuCount * 5 );
        int threads   = map.intValue( "threads", cpuCount );

        /*
        this.taskQueue = new DistributedTaskQueue<IRubyObject>(capacity, threads) {

            @Override
            public void onResult(IRubyObject result) {
                if (getOnResultBlock() != null) {
                    IRubyObject[] args = {result};
                    ThreadContext context = getRuntime().getThreadService().getCurrentContext();
                    getOnResultBlock().call(context, args);
                }
            }

            @Override
            public void onError(Exception e) {
                if (getOnErrorBlock() != null) {
                    IRubyObject[] args = { friend.exceptions().toRubyException(e) };
                    ThreadContext context = getRuntime().getThreadService().getCurrentContext();
                    getOnErrorBlock().call(context, args);
                }
            }
        };
        */

        ResultHandler<IRubyObject> handler = new ResultHandler<IRubyObject>() {

            @Override
            public void onResult(IRubyObject result) {
                if (getOnResultBlock() != null) {
                    IRubyObject[] args = {result};
                    ThreadContext context = getRuntime().getThreadService().getCurrentContext();
                    getOnResultBlock().call(context, args);
                }
            }

            @Override
            public void onException(Exception e) {
                if (getOnErrorBlock() != null) {
                    IRubyObject[] args = { friend.exception(e).rubyException() };
                    ThreadContext context = getRuntime().getThreadService().getCurrentContext();
                    getOnErrorBlock().call(context, args);
                }
            }

        };

        this.taskQueue = new SimpleTaskQueue<IRubyObject>( capacity, threads, handler );

        return this;
    }

    /**
     * @return {@link #taskQueue}
     */
    private AbstractTaskQueue<IRubyObject> getTaskQueue() {
        return taskQueue;
    }

    /**
     * Set the exception handler. The given <u>block</u> will be called for each exception.
     * @param context jruby context
     * @param block will be called for each exception {@link smile.tasks.ResultHandler#onException(Exception)}
     */
    @JRubyMethod(name = "on_error")
    public void onError(ThreadContext context, Block block) {
        block.arity().checkArity( context.getRuntime(), 1 );
        this.onErrorBlock = block;
    }

    /**
     * @return {@link #onErrorBlock}
     */
    private Block getOnErrorBlock() {
        return onErrorBlock;
    }

    /**
     * Set the result handler. The given <u>block</u> will be called for each result.
     * @param context jruby context
     * @param block will be called for each result {@link smile.tasks.ResultHandler#onException(Exception)}
     */
    @JRubyMethod(name = "on_result")
    public void onResult(ThreadContext context, Block block) {
        block.arity().checkArity( context.getRuntime(), 1 );
        this.onResultBlock = block;
    }

    /**
     * @return {@link #onResultBlock}
     */
    private Block getOnResultBlock() {
        return onResultBlock;
    }

    /**
     *
     * @param context jruby context
     * @param args The given <u>block</u> will be invoked with this arguments
     * @param block will be submitted as {@link smile.tasks.jruby.JRubyTask}
     *
     * @see smile.tasks.AbstractTaskQueue#submit(java.util.concurrent.Callable)
     */
    @JRubyMethod(name = "submit", rest = true)
    public void submit(ThreadContext context, IRubyObject[] args, Block block) {

        JRubyTask callable = new JRubyTask(context.getRuntime(), args, block);
        getTaskQueue().submit(callable);
    }

    /**
     *
     * @param context jruby context
     * @see smile.tasks.AbstractTaskQueue#await()
     */
    @JRubyMethod(name = "await")
    public void await(ThreadContext context) {
        getTaskQueue().await();
    }

    /**
     *
     * @param context jruby context
     * @see smile.tasks.AbstractTaskQueue#cancel()
     */
    @JRubyMethod(name = "cancel")
    public void cancel(ThreadContext context) {
        getTaskQueue().cancel();
    }
}
