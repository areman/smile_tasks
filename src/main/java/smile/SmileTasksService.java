package smile;

import org.jruby.Ruby;
import org.jruby.runtime.load.BasicLibraryService;

import java.io.IOException;

/**
 * @author Andre Kullmann
 */
public class SmileTasksService implements BasicLibraryService {

    @Override
    public boolean basicLoad( Ruby runtime ) throws IOException {

        return true;
    }
}
