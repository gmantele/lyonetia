package cds.adql.validation.report;

import cds.adql.validation.parser.adql.ADQLVersion;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class QueryValidationReportTest {

    /* *************************************************************************
     * ADD_SUCCESSFUL_VALIDATION
     */

    @Test
    void addSuccessfulValidation_ShouldSucceed_WhenEver() {
        // Given:
        final String parserId = "MyParser";
        final ADQLVersion version = ADQLVersion.V2_0;
        final Long duration = 1002L;
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addSuccessfulValidation(parserId, version, duration);

        // Then:
        assertEquals(1, report.size());
        final Iterator<QueryValidationReportRecord> itRec = report.iterator();
        assertTrue(itRec.hasNext());
        final QueryValidationReportRecord rec = itRec.next();
        assertFalse(itRec.hasNext());
        assertTrue(rec.isValidationPassed());
        assertEquals(parserId, rec.getParserID());
        assertEquals(version, rec.getVersion());
        assertEquals(duration, rec.getDuration().orElse(null));
        assertTrue(rec.getErrorMessage().isEmpty());
    }

    @Test
    void addSuccessfulValidation_ShouldFail_WhenNoParserID() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When + Then:
        assertThrows(NullPointerException.class, ()->report.addSuccessfulValidation(null, ADQLVersion.V2_1, 0L));
    }


    /* *************************************************************************
     * ADD_FAILED_VALIDATION
     */

    @Test
    void addFailedValidation_ShouldSucceed_WhenEver() {
        // Given:
        final String parserId = "MyParser";
        final ADQLVersion version = ADQLVersion.V2_0;
        final Long duration = 1002L;
        final String error = "Blabla";
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addFailedValidation(parserId, version, duration, error);

        // Then:
        assertEquals(1, report.size());
        final Iterator<QueryValidationReportRecord> itRec = report.iterator();
        assertTrue(itRec.hasNext());
        final QueryValidationReportRecord rec = itRec.next();
        assertFalse(itRec.hasNext());
        assertFalse(rec.isValidationPassed());
        assertEquals(parserId, rec.getParserID());
        assertEquals(version, rec.getVersion());
        assertEquals(duration, rec.getDuration().orElse(null));
        assertEquals(error, rec.getErrorMessage().orElse(null));
    }


    /* *************************************************************************
     * IS_PASSED
     */

    @Test
    void isPassed_ShouldBeTrue_WhenOnlyOneRecordWhichIsSuccessful() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addSuccessfulValidation("MyParser", null, null);

        // Then:
        assertEquals(1, report.size());
        assertTrue(report.isPassed());
    }

    @Test
    void isPassed_ShouldBeTrue_WhenMoreThanOneRecordAndAllAreSuccessful() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addSuccessfulValidation("MyParser", null, null);
        report.addSuccessfulValidation("MyParser", null, null);
        report.addSuccessfulValidation("MyParser", null, null);

        // Then:
        assertEquals(3, report.size());
        assertTrue(report.isPassed());
    }

    @Test
    void isPassed_ShouldBeFalse_WhenOnlyOneRecordWhichIsFailed() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addFailedValidation("MyParser", null, null, null);

        // Then:
        assertEquals(1, report.size());
        assertFalse(report.isPassed());
    }

    @Test
    void isPassed_ShouldBeFalse_WhenMoreThanOneRecordAndAtLeastOneIsFailed() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addSuccessfulValidation("MyParser", null, null);
        report.addFailedValidation("MyParser", null, null, null);
        report.addSuccessfulValidation("MyParser", null, null);

        // Then:
        assertEquals(3, report.size());
        assertFalse(report.isPassed());
    }


    /* *************************************************************************
     * COUNT_PASSED
     */

    @Test
    void countPassed_ShouldSucceed_WhenOneFailed() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addSuccessfulValidation("MyParser", null, null);
        report.addFailedValidation("MyParser", null, null, null);
        report.addSuccessfulValidation("MyParser", null, null);

        // Then:
        assertEquals(3, report.size());
        assertEquals(2, report.countPassed());
    }

    @Test
    void countPassed_ShouldSucceed_WhenAllFailed() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addFailedValidation("MyParser", null, null, null);
        report.addFailedValidation("MyParser", null, null, null);
        report.addFailedValidation("MyParser", null, null, null);

        // Then:
        assertEquals(3, report.size());
        assertEquals(0, report.countPassed());
    }

    @Test
    void countPassed_ShouldSucceed_WhenAllPassed() {
        // Given:
        final QueryValidationReport report = new QueryValidationReport();

        // When:
        report.addSuccessfulValidation("MyParser", null, null);
        report.addSuccessfulValidation("MyParser", null, null);
        report.addSuccessfulValidation("MyParser", null, null);

        // Then:
        assertEquals(3, report.size());
        assertEquals(report.size(), report.countPassed());
    }
}