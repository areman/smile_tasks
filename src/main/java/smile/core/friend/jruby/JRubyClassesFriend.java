package smile.core.friend.jruby;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.runtime.builtin.IRubyObject;
import smile.core.util.StringUtils;

import java.util.List;

/**
 * @author Andre Kullmann
 */
public class JRubyClassesFriend {

    private final Ruby runtime;

    public JRubyClassesFriend(Ruby runtime) {
        this.runtime = runtime;
    }

    public RubyModule getModule(String path) {
        return getModule(StringUtils.split( path, "::" ) );
    }

    public RubyModule getModule( List<String> path ) {

        if( path.isEmpty() )
            return runtime.getObject();

        RubyModule m = runtime.getModule( path.get(0) ) ;

        for (int i = 1; i < path.size(); i++) {
            IRubyObject tmp = m.getConstantNoConstMissing( path.get(i) );
            if (tmp == null) {
                tmp = m.defineModuleUnder( path.get(i) );
            }
            m = (RubyModule) tmp;
        }

        return m;
    }

    /*
    private RubyClass getClass(Ruby runtime, Class<? extends RubyObject> klass ) {

        JRubyClass anno     = klass.getAnnotation( JRubyClass.class );
        List<String> path   = split( anno.name()[0] );
        return getClass( runtime, path );
    }

    private RubyClass getClass(Ruby runtime, String...path) {
        return getClass(runtime, Arrays.asList(path) );
    }
    */

    public RubyClass getClass(String path) {
        return getClass(StringUtils.split(path, "::"));
    }


    public RubyClass getClass( List<String> path ) {
        if (path.size() == 1) {
            return runtime.getClass( path.get(0) );
        }
        RubyModule m = runtime.getModule( path.get(0) );

        for (int i = 1; i < path.size() - 1; i++) {
            IRubyObject tmp = m.getConstant( path.get(i) );
            if (tmp == null) {
                tmp = m.defineModuleUnder( path.get(i) );
            }
            m = (RubyModule) tmp;
        }

        return m.getClass( path.get( path.size() - 1) );
    }
}
