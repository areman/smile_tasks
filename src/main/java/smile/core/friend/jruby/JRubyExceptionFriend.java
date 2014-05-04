package smile.core.friend.jruby;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyException;
import org.jruby.RubyString;
import org.jruby.exceptions.RaiseException;

/**
 * @author Andre Kullmann
 */
public class JRubyExceptionFriend extends JRubyAbstractFriend {

    private final Exception exception;

    public JRubyExceptionFriend(Ruby runtime, Exception exception) {
        super( runtime );
        this.exception = exception;
    }

    public RubyException rubyException() {
        RubyException exception = findRubyException( this.exception );
        return exception == null ? newRubyException( this.exception ) : exception;
    }

    private RubyException findRubyException( Throwable t ) {
        for( Throwable e = t; e != null; e = e.getCause() ) {
            if( e instanceof RaiseException ) {
                return ((RaiseException) e).getException();
            }
        }
        return null;
    }

    private RubyException newRubyException( Throwable t ) {

        RubyException exception =
                RubyException.newException( getRuntime(), getRuntime().getStandardError(), t.getMessage() );
        //BacktraceData.
        exception.set_backtrace( toStringArray( t.getStackTrace() ) );

        return exception;
    }

    private RubyArray toStringArray( StackTraceElement[] elements ) {

        RubyArray array = RubyArray.newArray( getRuntime() );
        for (StackTraceElement element : elements) {
            array.add(toString( element));
        }
        return array;
    }

    private RubyString toString( StackTraceElement element ) {

        return getRuntime().newString(element.toString());
    }
}
