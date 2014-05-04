smile_tasks
===========

a simple library for parallel processing. designed for batch processing.

the idea behind smile_tasks is simple and clear.
in your batch programm you can create a task queue, and submit work to do.
the work will be done in parallel in different threads or remote on other servers.
the queue can receives the result of the work and invokes callbacks.
your batch programm can process the results in synchronized order to write files.
