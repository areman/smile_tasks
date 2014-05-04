package smile.core.friend.jruby;

import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Andre Kullmann
 */
public class JRubyArrayFriend extends JRubyAbstractFriend {

    private final IRubyObject[] array;

    public JRubyArrayFriend(Ruby runtime, IRubyObject[] array) {
        super( runtime );
        this.array = array;
    }

    public JRubyHashFriend mapFriend(int index) {

        if( array.length <= index ) {
            return new JRubyHashFriend( RubyHash.newHash( getRuntime() ) );
        }

        if( array[index] instanceof RubyHash ) {
            return new JRubyHashFriend( (RubyHash) array[index] );
        } else {
            throw getRuntime().newTypeError("parameter at " + index + " must be a Hash.");
        }
    }
}
