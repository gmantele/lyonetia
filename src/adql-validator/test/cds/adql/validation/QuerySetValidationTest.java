package cds.adql.validation;

import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.ADQLValidationReport;
import cds.adql.validation.report.QueryValidationReport;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuerySetValidationTest {

    @Test
    void run_ShouldReturnAnEmptyReport_WhenNoQuery() throws IncorrectValidatorConfigurationException {
        // Given:
        final ValidationSet set               = new ValidationSet("test");
        final ThreadPool pool                 = new ThreadPool(1);
        final ADQLValidationReport report     = new ADQLValidationReport();
        final QuerySetValidation setValidator = new QuerySetValidation(set, pool, report);

        // When:
        setValidator.startValidation();
        pool.shutdown();

        // Then:
        assertTrue(pool.isStopped());
        assertFalse(report.getQueryReports().hasNext());

    }

    @Test
    void run_ShouldSucceed_WhenSomeQueries() throws IncorrectValidatorConfigurationException {
        // Given:
        final ValidationSet set               = new ValidationSet("test");
        set.add(createQuery());
        set.add(createQuery());
        final ThreadPool pool                 = new ThreadPool(1);
        final ADQLValidationReport report     = new ADQLValidationReport();
        final QuerySetValidation setValidator = new QuerySetValidation(set, pool, report);

        // When:
        setValidator.startValidation();
        pool.shutdown();

        // Then:
        assertTrue(pool.isStopped());
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itReports = report.getQueryReports();
        for(int i=0; i<2; i++) {
            assertTrue(itReports.hasNext());
            final Map.Entry<UUID, QueryValidationReport> reportEntry = itReports.next();
            assertEquals(3, reportEntry.getValue().size());

            // TODO finish debugging!
            reportEntry.getValue().forEach(record -> System.out.println(record.getParserID()+": "+record.getDuration()+"ms ; passed:"+record.isValidationPassed()+" ; error("+record.getErrorMessage()+")"));
            assertEquals(2, reportEntry.getValue().countPassed());
        }
        assertFalse(itReports.hasNext());
    }

    private ValidationQuery createQuery(){
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT dumb_query");
        query.setExpectedToBeValid(true);
        return query;
    }
}