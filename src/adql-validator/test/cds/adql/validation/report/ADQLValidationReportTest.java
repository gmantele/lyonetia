package cds.adql.validation.report;

import cds.adql.validation.parser.adql.ParsingResult;
import cds.adql.validation.parser.adql.exceptions.ADQLParseException;
import cds.adql.validation.query.ValidationQuery;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ADQLValidationReportTest {

    /* *************************************************************************
     * ADD_SUCCESSFUL_VALIDATION
     */

    @Test
    void addSuccessfulValidation_ShouldCreateAReport_WhenNoneExists() {
        // Given:
        final UUID queryId = UUID.randomUUID();
        final ADQLValidationReport report = new ADQLValidationReport();
        assertFalse(report.getQueryReports().hasNext());

        // When:
        report.addSuccessfulValidation(new ValidationQuery(queryId), "MyParser", new ParsingResult());

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertFalse(itQueryReports.hasNext());
        assertEquals(queryId, queryReportEntry.getKey());
        assertEquals(1, queryReportEntry.getValue().size());
        assertTrue(queryReportEntry.getValue().isPassed());
    }

    @Test
    void addSuccessfulValidation_ShouldUpdateAReport_WhenOneAlreadyExists() {
        // Given:
        final UUID queryId = UUID.randomUUID();
        final ADQLValidationReport report = new ADQLValidationReport();
        assertFalse(report.getQueryReports().hasNext());

        // When:
        report.addSuccessfulValidation(new ValidationQuery(queryId), "MyParser1", new ParsingResult());
        report.addSuccessfulValidation(new ValidationQuery(queryId), "MyParser2", new ParsingResult());

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertFalse(itQueryReports.hasNext());
        assertEquals(queryId, queryReportEntry.getKey());
        assertEquals(2, queryReportEntry.getValue().size());
        assertTrue(queryReportEntry.getValue().isPassed());
    }


    /* *************************************************************************
     * ADD_FAILED_VALIDATION
     */

    @Test
    void addFailedValidation_ShouldCreateAReport_WhenNoneExists() {
        // Given:
        final UUID queryId = UUID.randomUUID();
        final ADQLValidationReport report = new ADQLValidationReport();
        assertFalse(report.getQueryReports().hasNext());

        // When:
        report.addFailedValidation(new ValidationQuery(queryId), "MyParser", new ParsingResult());

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertFalse(itQueryReports.hasNext());
        assertEquals(queryId, queryReportEntry.getKey());
        assertEquals(1, queryReportEntry.getValue().size());
        assertFalse(queryReportEntry.getValue().isPassed());
    }

    @Test
    void addFailedValidation_ShouldUpdateAReport_WhenOneAlreadyExists() {
        // Given:
        final UUID queryId = UUID.randomUUID();
        final ADQLValidationReport report = new ADQLValidationReport();
        assertFalse(report.getQueryReports().hasNext());

        // When:
        report.addFailedValidation(new ValidationQuery(queryId), "MyParser1", new ParsingResult());
        report.addFailedValidation(new ValidationQuery(queryId), "MyParser2", new ParsingResult());

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertFalse(itQueryReports.hasNext());
        assertEquals(queryId, queryReportEntry.getKey());
        assertEquals(2, queryReportEntry.getValue().size());
        assertFalse(queryReportEntry.getValue().isPassed());
    }


    /* *************************************************************************
     * ADD_GRAVE_FAILURE
     */

    @Test
    void addGraveFailure_ShouldCreateAReport_WhenNoneExists() {
        // Given:
        final UUID queryId = UUID.randomUUID();
        final ADQLValidationReport report = new ADQLValidationReport();
        assertFalse(report.getQueryReports().hasNext());

        // When:
        report.addGraveFailure(new ValidationQuery(queryId), "MyParser", new ADQLParseException("test error"));

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertFalse(itQueryReports.hasNext());
        assertEquals(queryId, queryReportEntry.getKey());
        assertEquals(1, queryReportEntry.getValue().size());
        assertFalse(queryReportEntry.getValue().isPassed());
    }

    @Test
    void addGraveFailure_ShouldUpdateAReport_WhenOneAlreadyExists() {
        // Given:
        final UUID queryId = UUID.randomUUID();
        final ADQLValidationReport report = new ADQLValidationReport();
        assertFalse(report.getQueryReports().hasNext());

        // When:
        report.addGraveFailure(new ValidationQuery(queryId), "MyParser1", new ADQLParseException("test error"));
        report.addGraveFailure(new ValidationQuery(queryId), "MyParser2", new ADQLParseException("test error"));

        // Then:
        final Iterator<Map.Entry<UUID, QueryValidationReport>> itQueryReports = report.getQueryReports();
        assertTrue(itQueryReports.hasNext());
        final Map.Entry<UUID, QueryValidationReport> queryReportEntry = itQueryReports.next();
        assertFalse(itQueryReports.hasNext());
        assertEquals(queryId, queryReportEntry.getKey());
        assertEquals(2, queryReportEntry.getValue().size());
        assertFalse(queryReportEntry.getValue().isPassed());
    }
}