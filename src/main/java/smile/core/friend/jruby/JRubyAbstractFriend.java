package smile.core.friend.jruby;

import org.jruby.Ruby;

/**
 * @author Andre Kullmann
 */
public class JRubyAbstractFriend {

    private final Ruby runtime;

    public JRubyAbstractFriend(Ruby runtime) {
        this.runtime = runtime;
    }

    public Ruby getRuntime() {
        return runtime;
    }
}
