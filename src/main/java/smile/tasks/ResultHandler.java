package smile.tasks;

/**
 * @author Andre Kullmann
 */
public interface ResultHandler<T> {

    public void onResult( T result );

    public void onException( Exception e );
}
