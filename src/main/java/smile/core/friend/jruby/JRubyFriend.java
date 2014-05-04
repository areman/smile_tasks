package smile.core.friend.jruby;

import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Andre Kullmann
 */
public class JRubyFriend {

    public static JRubyFriend friend( Ruby runtime ) {
        return new JRubyFriend( runtime );
    }

    private final Ruby runtime;

    private JRubyFriend(Ruby runtime) {
        this.runtime = runtime;
    }

    public Ruby getRuntime() {
        return runtime;
    }


    public JRubyArrayFriend array( IRubyObject[] args ) {
        return new JRubyArrayFriend( getRuntime(), args );
    }

    public JRubyExceptionFriend exception(Exception exception) {
        return new JRubyExceptionFriend( getRuntime(), exception );
    }

    public JRubyClassFriend clazz( Class<? extends RubyObject> clazz ) {
        return new JRubyClassFriend( getRuntime(), clazz );
    }

    public JRubyClassesFriend classes() {
        return new JRubyClassesFriend( getRuntime() );
    }

    public JRubyThreadFriend thread( ThreadContext context ) {
        return new JRubyThreadFriend( context );
    }
}
