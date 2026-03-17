package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.report.ADQLValidationReport;
import cds.adql.validation.report.QueryValidationReport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SingleQueryValidationTest {

    static ADQLParser goodParser = null;
    static ADQLParser badParser  = null;

    static ThreadPool threadPool = null;

    @BeforeAll
    static void beforeAll() {
        threadPool = new ThreadPool(10);
        goodParser = new ADQLParser("MyGoodParser", "src/test/resources/parsers/successful_parsing.bash");
        badParser  = new ADQLParser("MyBadParser" , "src/test/resources/parsers/failed_parsing.bash");
    }

    @AfterAll
    static void afterAll() {
        threadPool.stop();
    }

    @Test
    void run_ShouldReportSuccess_WhenTheParsingSucceededAndItWasExpected() {
        // Given:
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        query.setExpectedToBeValid(true);
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, goodParser, report);

        // When:
        validator.run();

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertEquals(query.getId(), queryReportEntry.getKey());
        final QueryValidationReport queryReport = queryReportEntry.getValue();
        assertTrue(queryReport.isPassed());
    }

    @Test
    void run_ShouldReportFailure_WhenTheParsingSucceededAndItWasNotExpected() {
        // Given:
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        query.setExpectedToBeValid(false);
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, goodParser, report);

        // When:
        validator.run();

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertEquals(query.getId(), queryReportEntry.getKey());
        final QueryValidationReport queryReport = queryReportEntry.getValue();
        assertFalse(queryReport.isPassed());
    }

    @Test
    void run_ShouldReportSuccess_WhenTheParsingFailedAndItWasExpected() {
        // Given:
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        query.setExpectedToBeValid(false);
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, badParser, report);

        // When:
        validator.run();

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertEquals(query.getId(), queryReportEntry.getKey());
        final QueryValidationReport queryReport = queryReportEntry.getValue();
        assertTrue(queryReport.isPassed());
    }

    @Test
    void run_ShouldReportFailure_WhenTheParsingFailedAndItWasNotExpected() {
        // Given:
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        query.setExpectedToBeValid(true);
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, badParser, report);

        // When:
        validator.run();

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertEquals(query.getId(), queryReportEntry.getKey());
        final QueryValidationReport queryReport = queryReportEntry.getValue();
        assertFalse(queryReport.isPassed());
    }

    @Test
    void run_ShouldReportGraveFailure_WhenNoADQLQueryToParse() {
        // Given:
        final ValidationQuery query = new ValidationQuery();
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, badParser, report);

        // When:
        validator.run();

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertEquals(query.getId(), queryReportEntry.getKey());
        final QueryValidationReport queryReport = queryReportEntry.getValue();
        assertFalse(queryReport.isPassed());
        assertEquals("No ADQL query to parse!", queryReport.iterator().next().getErrorMessage().orElse(null));
    }

    @Test
    void run_ShouldReportGraveFailure_WhenIncorrectOutput() {
        // Given:
        final ValidationQuery query = new ValidationQuery();
        query.setQuery("SELECT foo");
        query.setExpectedToBeValid(true);
        final ADQLValidationReport report = new ADQLValidationReport();
        final SingleQueryValidation validator = new SingleQueryValidation(query, new ADQLParser("VeryBadParser", "echo"), report);

        // When:
        validator.run();

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertEquals(query.getId(), queryReportEntry.getKey());
        final QueryValidationReport queryReport = queryReportEntry.getValue();
        assertFalse(queryReport.isPassed());
        assertEquals("Incorrect output document! Expected a correctly formatted JSON object.", queryReport.iterator().next().getErrorMessage().orElse(null));
    }
}