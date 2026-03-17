package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.ADQLValidationReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuerySetValidationTest {

    private static List<ADQLParser> parsers;

    @BeforeAll
    static void beforeAll() throws IncorrectValidatorConfigurationException {
        parsers = ParsersConfiguration.getParsers();
    }

    @Test
    void run_ShouldReturnAnEmptyReport_WhenNoQuery() {
        // Given:
        final ValidationSet set               = new ValidationSet("test");
        final ThreadPool pool                 = new ThreadPool(1);
        final ADQLValidationReport report     = new ADQLValidationReport();
        final QuerySetValidation setValidator = new QuerySetValidation(set, pool, parsers, report);

        // When:
        setValidator.startValidation();
        pool.shutdown();

        // Then:
        assertTrue(pool.isStopped());
        assertFalse(report.getQueryReports().hasNext());

    }

    @Test
    void run_ShouldSucceed_WhenSomeQueries() {
        // Given:
        final ValidationSet set               = new ValidationSet("test");
        set.add(createQuery());
        set.add(createQuery());
        final ThreadPool pool                 = new ThreadPool(1);
        final ADQLValidationReport report     = new ADQLValidationReport();
        final QuerySetValidation setValidator = new QuerySetValidation(set, pool, parsers, report);

        // When:
        setValidator.startValidation();
        pool.shutdown();

        // Then:
        assertTrue(pool.isStopped());
        ReportToolBox.checkCounts(report, 2, 4); // because 2 queries with 3 parsers/executions each, so 6 executions ; one out of the three parsers is always successful, so: 2*(1 ok, 2 ko) = (2 ok, 4 ko)
    }

    private ValidationQuery createQuery(){
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT dumb_query");
        query.setExpectedToBeValid(true);
        return query;
    }
}