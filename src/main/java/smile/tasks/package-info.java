/**
 * This package contains a simple implementation for parallel task execution in batch jobs.
 *
 * What's the idea ?
 *
 * In you batch job you just create a instance of {@link smile.tasks.AbstractTaskQueue} ( if you use only one jvm
 * use the {@link smile.tasks.SimpleTaskQueue} implementation.
 * You can submit {@link java.util.concurrent.Callable} objects to you {@link smile.tasks.AbstractTaskQueue}, the
 * execution will by done by the {@link java.util.concurrent.ExecutorService} ( see {@link smile.tasks.AbstractTaskQueue#getWorkerPool()} ).
 * In batch processing you can't just fire your callable and forget them. In most cases you need to wait for them and evaluate there result.
 * This can be done with an {@link smile.tasks.ResultHandler}. And that's one of the imported features, the {@link smile.tasks.ResultHandler}
 * is allays invoked on the {@link java.lang.Thread} where the instance of the {@link smile.tasks.AbstractTaskQueue} was created.
 * So you don't run in synchronization trouble, will handling the results ( maybe writing a file ).
 *
 * You {@link smile.tasks.AbstractTaskQueue} will dispatch the submitted {@link java.util.concurrent.Callable} object to
 * a {@link java.util.concurrent.ExecutorService} and the {@link smile.tasks.ResultHandler} will always be called with
 * results of {@link java.util.concurrent.Callable#call()} on the owner thread.
 *
 * @author Andre Kullmann
 */
package smile.tasks;