package smile.core.friend.jruby;

import org.jruby.RubyHash;
import org.jruby.RubyNumeric;
import org.jruby.RubySymbol;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;

/**
 * @author Andre Kullmann
 */
public class JRubyThreadFriend extends JRubyAbstractFriend {

    private final ThreadContext context;

    public JRubyThreadFriend(ThreadContext context) {
        super( context.getRuntime() );
        this.context = context;
    }

    public JRubyHashFriend mapFriend(String key) {
        RubySymbol symbol = RubySymbol.newSymbol( getRuntime(), key);
        RubyHash hash = (RubyHash) context.getThread().op_aref( symbol );
        return new JRubyHashFriend( hash );
    }

    public void set( String key, RubyHash hash ) {
        RubySymbol symbol = RubySymbol.newSymbol( getRuntime(), key);
        context.getThread().op_aset(symbol, hash);
    }


    public String stringValue( String key ) {
        RubySymbol symbol  = RubySymbol.newSymbol( getRuntime(), key);
        IRubyObject object = context.getThread().op_aref(symbol);
        return object.isNil() ? null : object.asJavaString();
    }

    public Integer intValue( String key ) {
        RubySymbol symbol  = RubySymbol.newSymbol( getRuntime(), key);
        IRubyObject object = context.getThread().op_aref(symbol);
        return object == null || object.isNil() ? null : RubyNumeric.num2int( object );
    }

    public Long longValue( String key ) {
        RubySymbol symbol  = RubySymbol.newSymbol( getRuntime(), key);
        IRubyObject object = context.getThread().op_aref(symbol);
        return object == null || object.isNil() ? null : RubyNumeric.num2long(object);
    }

    public Boolean boolValue( String key ) {
        RubySymbol symbol  = RubySymbol.newSymbol( getRuntime(), key);
        IRubyObject object = context.getThread().op_aref(symbol);
        return object == null || object.isNil() ? null : object.isTrue();
    }

    public JRubyHashFriend contextHash() {

        final RubyHash hash = RubyHash.newHash( getRuntime() );
        final List<?>  keys = context.getThread().keys().getList();

        for( Object key : keys ) {
            hash.put( key, context.getThread().op_aref( (IRubyObject) key) );
        }

        return new JRubyHashFriend(hash);
    }

}
