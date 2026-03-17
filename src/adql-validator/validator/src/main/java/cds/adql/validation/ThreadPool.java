package cds.adql.validation;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Thread pool for the ADQL validator.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (05/2025)
 */
public class ThreadPool implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ThreadPool.class.getName());

    private static final int DEFAULT_TIME_BEFORE_DESTRUCTION = 10; // seconds

    private final int timeBeforeDestruction; // seconds

    private final ExecutorService poolQueries;

    /**
     * Create a Thread pool with the given maximum number or running queries.
     *
     * @param maxNbRunningQueries Maximum number of queries that can startValidation
     *                            simultaneously. It must be &gt; 0.
     *
     * @throws IllegalArgumentException If maxNbRunningQueries &le; 0.
     */
    public ThreadPool(final int maxNbRunningQueries) throws IllegalArgumentException {
        this(maxNbRunningQueries, -1);
    }

    /**
     * Create a Thread pool with the given maximum number or running queries.
     *
     * @param maxNbRunningQueries                 Maximum number of queries that
     *                                            can startValidation simultaneously.
     *                                            It must be &gt; 0.
     * @param maxWaitingTimeBeforePoolDestruction Maximum time (in seconds)
     *                                            before forcing destruction of
     *                                            all threads when stopping the
     *                                            pool. By default, or if
     *                                            negative,
     *                                            {@value #DEFAULT_TIME_BEFORE_DESTRUCTION} seconds.
     *
     *
     * @throws IllegalArgumentException If maxNbRunningQueries &le; 0.
     */
    public ThreadPool(final int maxNbRunningQueries, final int maxWaitingTimeBeforePoolDestruction) throws IllegalArgumentException {
        poolQueries = Executors.newFixedThreadPool(maxNbRunningQueries);

        if (maxWaitingTimeBeforePoolDestruction < 0)
            timeBeforeDestruction = DEFAULT_TIME_BEFORE_DESTRUCTION;
        else
            timeBeforeDestruction = maxWaitingTimeBeforePoolDestruction;
    }

    /**
     * Submit the given validation for execution. If a Thread is available, it
     * will startValidation immediately. Otherwise, it will be queued and will startValidation whenever
     * it is possible.
     *
     * @param validation    The validation to startValidation.
     *
     * @return  A {@link Future} representing pending completion of the
     *          given validation.
     *
     * @throws RejectedExecutionException   If the validation cannot be
     *                                      scheduled for execution.
     * @throws NullPointerException         If the validation is
     *                                      <code>null</code>.
     */
    public Future<?> submit(final SingleQueryValidation validation) throws RejectedExecutionException, NullPointerException {
        return poolQueries.submit(validation);
    }

    /**
     * Initiate the shutdown process. This function waits for the end of
     * execution of all submitted tasks. No new task can be submitted.
     *
     * <p>
     *     This function waits the time set at the creation of this ThreadPool
     *     (see {@link #ThreadPool(int, int)}). If not set at initialization,
     *     it is set by default to {@value #DEFAULT_TIME_BEFORE_DESTRUCTION}
     *     seconds.
     * </p>
     */
    public void shutdown(){
        LOGGER.info(() -> "Gracefully shutting down the queries...");

        poolQueries.shutdown();

        if (waitForCompletion())
            LOGGER.info(() -> "All queries successfully completed.");
        else
            LOGGER.warning(() -> "Some queries are still running!");
    }

    private boolean waitForCompletion(){
        try{
            return poolQueries.awaitTermination(timeBeforeDestruction, TimeUnit.SECONDS);
        }
        catch(InterruptedException ie){
            Thread.currentThread().interrupt();
            LOGGER.severe("Process destruction interrupted! Cause: "+ie.getMessage());
        }
        catch(Exception e) {
            LOGGER.severe("Process destruction error! Cause: "+e.getMessage());
        }
        return false;
    }

    /**
     * Tell whether no more thread is running.
     *
     * @return  <code>true</code> when no more thread runs,
     *          <code>false</code> if at least one is still running.
     */
    public boolean isStopped(){
        return poolQueries.isTerminated();
    }

    /**
     * Convenient function inherited from {@link AutoCloseable}.
     * It just calls {@link #stop()}.
     */
    @Override
    public final void close() {
        stop();
    }

    /**
     * Stop properly this thread pool.
     *
     * <p>
     *     This function first tries to stop gracefully this pool by calling
     *     {@link #shutdown()}. Then, if not yet stopped, the running threads
     *     are stopped forcefully. Both steps include a waiting time. This time
     *     is configured when creating the pool ({@link #ThreadPool(int, int)} ).
     * </p>
     */
    public void stop(){
        shutdown();
        stopQueries();
    }

    private void stopQueries(){
        if (!poolQueries.isTerminated())
        {
            LOGGER.severe(() -> "Attempting to stop forcibly all running queries...");

            final List<Runnable> waitingThreads = poolQueries.shutdownNow();
            LOGGER.info(() -> waitingThreads.size()+" queries were in the queue. They will never be executed.");

            lastChanceWaitingForCompletion();
        }
    }

    private void lastChanceWaitingForCompletion(){
        try
        {
            if (poolQueries.awaitTermination(timeBeforeDestruction, TimeUnit.SECONDS))
                LOGGER.info(() -> "All queries are now stopped.");
            else
                LOGGER.severe(() -> "Failed to stop all running queries!");
        }
        catch(InterruptedException ie){
            Thread.currentThread().interrupt();
            LOGGER.severe("Process destruction interrupted! Cause: "+ie.getMessage());
        }
        catch(Exception e) {
            LOGGER.severe("Process destruction error! Cause: "+e.getMessage());
        }
    }

}
