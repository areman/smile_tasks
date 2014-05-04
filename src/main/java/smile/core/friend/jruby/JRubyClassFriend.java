package smile.core.friend.jruby;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyClass;
import org.jruby.runtime.ObjectAllocator;
import smile.core.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Andre Kullmann
 */
public class JRubyClassFriend extends JRubyAbstractFriend {

    private final Class<? extends RubyObject> rubyClass;

    public JRubyClassFriend(Ruby runtime, Class<? extends RubyObject> rubyClass) {
        super( runtime );
        this.rubyClass = rubyClass;
    }

    public RubyClass define() {
        Field field;
        try {
            field = rubyClass.getField("ALLOCATOR");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        ObjectAllocator allocator;
        try {
            allocator = (ObjectAllocator) field.get( null );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return define( allocator );
    }

    public RubyClass define( ObjectAllocator allocator ) {

        JRubyClass anno     = rubyClass.getAnnotation( JRubyClass.class );

        if( anno.name().length != 1 ) {
            throw new RuntimeException( "The name attribute of JRubyClass annotation must have a length of 1. Class:" + rubyClass.getName()  );
        }

        List<String> path   = StringUtils.split(anno.name()[0], "::");
        String name         = path.remove( path.size()-1 );
        List<String> parent = StringUtils.split(anno.parent(), "::");

        JRubyClassesFriend classes = JRubyFriend.friend( getRuntime() ).classes();

        //System.out.println("PATH " + path );
        RubyModule module = classes.getModule(path);
        RubyClass result = module.defineClassUnder( name, classes.getClass(parent), allocator );

        for( String i : anno.include() ) {
            result.includeModule( classes.getModule(StringUtils.split(i, "::")) );
        }

        result.defineAnnotatedMethods( rubyClass );

        result.defineAnnotatedConstants( rubyClass );

        return result;
    }



}
