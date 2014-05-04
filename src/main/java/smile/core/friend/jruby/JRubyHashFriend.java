package smile.core.friend.jruby;

import org.jruby.RubyBoolean;
import org.jruby.RubyHash;
import org.jruby.RubyNumeric;
import org.jruby.RubySymbol;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * @author Andre Kullmann
 */
public class JRubyHashFriend extends JRubyAbstractFriend {

    private final RubyHash hash;

    public JRubyHashFriend(RubyHash hash) {
        super( hash.getRuntime() );
        this.hash = hash;
    }

    public Boolean boolValue( String key  ) {
        IRubyObject value = value( key );
        if( value == null || value.isNil() ) {
            return null;
        }
        if( value instanceof RubyBoolean) {
            return value.isTrue();
        }
        throw getRuntime().newTypeError("unable to convert " + value.getClass().getName() + " to boolean.");
    }

    public Boolean boolValue( String key, boolean defaultValue ) {

        Boolean value = boolValue( key );
        return value == null ? defaultValue : value;
    }

    public Integer intValue( String key, int defaultValue ) {

        IRubyObject value = value(key);
        if( value == null ) {
            return defaultValue;
        }
        if( value instanceof Number ) {
            return ((Number)value).intValue();
        }
        if( value instanceof RubyNumeric) {
            return RubyNumeric.num2int( value );
        }
        if( value.isNil() ) {
            return defaultValue;
        }
        throw getRuntime().newTypeError("unable to convert " + value.getClass().getName() + " to int.");
    }

    public JRubyHashFriend hashFriend(String key) {

        RubyHash hash = (RubyHash) value( key );
        return new JRubyHashFriend( hash );
    }


    public IRubyObject value( String key ) {
        RubySymbol symbol = getRuntime().newSymbol(key);
        IRubyObject value = (IRubyObject) hash.get( symbol );
        return value;
    }

}
