package cds.adql.validation.report;

import cds.adql.validation.parser.adql.ADQLVersion;
import cds.adql.validation.parser.validationset.ValidationSetParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryValidationReportRecordTest {

    @Test
    void create_ShouldFail_WhenNoParserID() {
        // Given + When:
        try {
            new QueryValidationReportRecord(null, ADQLVersion.V2_1, true, 0L, "Blabla");
            fail("It MUST fail when no parser ID is provided!");
        }
        // Then:
        catch(Throwable t) {
            assertEquals(NullPointerException.class, t.getClass());
            assertEquals("ID of the used parser is required!", t.getMessage());
        }

    }


    /* *************************************************************************
     * GET_VERSION
     */

    @Test
    void getVersion_ShouldSucceed_WhenOneIsProvided() {
        // Given + When:
        final ADQLVersion expectedVersion = ADQLVersion.V2_0;
        final QueryValidationReportRecord rec = new QueryValidationReportRecord("Hi", expectedVersion, true, 0L, null);

        // Then:
        assertEquals(ADQLVersion.V2_0, rec.getVersion());
    }

    @Test
    void getVersion_ShouldReturnDefaultVersion_WhenNoneIsProvided() {
        // Given + When:
        final QueryValidationReportRecord rec = new QueryValidationReportRecord("Hi", null, true, 0L, null);

        // Then:
        assertEquals(ValidationSetParser.DEFAULT_ADQL_VERSION, rec.getVersion());
    }


    /* *************************************************************************
     * GET_ERROR_MESSAGE
     */

    @Test
    void getErrorMessage_ShouldBeNull_WhenEmptyMessage() {
        // Given + When:
        final QueryValidationReportRecord rec = new QueryValidationReportRecord("Hi", null, true, 0L, "  ");

        // Then:
        assertTrue(rec.getErrorMessage().isEmpty());
    }

    @Test
    void getErrorMessage_ShouldTrim_WhenSpacesAround() {
        // Given + When:
        final String message = "blabla";
        final QueryValidationReportRecord rec = new QueryValidationReportRecord("Hi", null, true, 0L, "\t"+message+"  ");

        // Then:
        assertTrue(rec.getErrorMessage().isPresent());
        assertEquals(message, rec.getErrorMessage().orElse(null));
    }
}