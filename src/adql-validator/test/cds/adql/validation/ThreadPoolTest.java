package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.report.ADQLValidationReport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {

    /* *************************************************************************
     * SUBMIT
     */

    @Test
    void submit_ShouldSucceed_WhenNonNullValidator() throws Exception {
        // Given:
        try(final ThreadPool pool = new ThreadPool(1)) {
            final ValidationQuery query = new ValidationQuery();
            final ADQLParser parser = new ADQLParser("foo", "echo");
            final ADQLValidationReport report = new ADQLValidationReport();
            final SingleQueryValidation validator = new SingleQueryValidation(query, parser, report);

            // When:
            final Future<?> futureValidation = pool.submit(validator);
            Awaitility.await().atMost(1, TimeUnit.SECONDS)
                              .until(futureValidation::isDone);

            // Then:
            assertTrue(futureValidation.isDone());
            assertTrue(report.getQueryReports().hasNext());
        }
    }

    /* *************************************************************************
     * SHUTDOWN
     */

    @Test
    void shutdown_ShouldSucceed_WhenNothingIsRunning() {
        // Given:
        final ThreadPool pool = new ThreadPool(1);

        // When:
        pool.shutdown();

        // Then:
        assertTrue(pool.isStopped());
    }

    @Test
    void shutdown_ShouldPreventSubmission_WhenEver() {
        // Given:
        final ThreadPool pool = new ThreadPool(1);
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        final ADQLParser parser = new ADQLParser("foo", "test-resources/parsers/long_running.bash");
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, parser, report);

        // When:
        pool.shutdown();

        // Then:
        assertThrows(RejectedExecutionException.class, () -> pool.submit(validator));
        assertTrue(pool.isStopped());
    }

    @Test
    void shutdown_ShouldFailAndPreventSubmission_WhenLongRunningQuery() {
        // Given:
        final ThreadPool pool = new ThreadPool(1, 1);
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        final ADQLParser parser = new ADQLParser("foo", "test-resources/parsers/long_running.bash");
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, parser, report);
        final Future<?> futureValidation = pool.submit(validator);

        // When:
        pool.shutdown();

        // Then:
        assertFalse(futureValidation.isDone());
        assertFalse(pool.isStopped());
        assertThrows(RejectedExecutionException.class, () -> pool.submit(validator));
    }

    /* *************************************************************************
     * STOP
     */

    @Test
    void stop_ShouldSucceed_WhenNothingIsRunning() {
        // Given:
        final ThreadPool pool = new ThreadPool(1);

        // When:
        pool.stop();

        // Then:
        assertTrue(pool.isStopped());
    }

    @Test
    void stop_ShouldSucceed_WhenLongRunningQuery() {
        // Given:
        final int TIME_BEFORE_DESTRUCTION = 1; // seconds
        final ThreadPool pool = new ThreadPool(1, TIME_BEFORE_DESTRUCTION);
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        final ADQLParser parser = new ADQLParser("foo", "test-resources/parsers/long_running.bash");
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, parser, report);
        final Future<?> futureValidation = pool.submit(validator);

        // When:
        pool.stop();

        // Then:
        assertThrows(RejectedExecutionException.class, () -> pool.submit(validator));
        assertTrue(futureValidation.isDone());
        assertTrue(pool.isStopped());
    }
}