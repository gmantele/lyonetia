package cds.adql.validation.parser.adql;

import cds.adql.validation.ThreadPool;
import cds.adql.validation.parser.adql.exceptions.ADQLParseException;
import cds.adql.validation.parser.adql.exceptions.RunParserException;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version (04/2025)
 */
class ADQLParserTest {

    static ThreadPool threadPool = null;
    static final String DUMB_QUERY    = "SELECT something FROM something_else";

    @BeforeAll
    static void beforeAll() {
        threadPool = new ThreadPool(10);
    }

    @AfterAll
    static void afterAll() {
        threadPool.stop();
    }


    /* **********************************************************************
    *  GET_NAME */

    @Test
    void getName_ShouldSucceed_WhenNonEmptyString() {
        // Given:
        final String expectedParserName = "bla bla";

        // When:
        final ADQLParser parser = new ADQLParser(" "+expectedParserName+"  ", "echo");

        // Then:
        assertEquals(expectedParserName, parser.getName());
    }

    @Test
    void getName_ShouldNotBePossible_WhenEmptyString() {
        // Given:
        for(String emptyName : new String[]{null, "", " ", "\t", "\n"})
            // When + Then:
            assertThrows(NullPointerException.class, ()->new ADQLParser(emptyName, "echo"));
    }


    /* **********************************************************************
     *  PARSE */

    @Test
    void parse_ShouldFail_WhenCommandDoesNotReturnAJSONDoc() {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "echo");

        // When:
        try {
            parser.parse(DUMB_QUERY, ADQLVersion.V2_0);
            fail("This test should have failed because the command does not return a JSON document!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(ADQLParseException.class, ex.getClass());
            assertEquals(JSONException.class, ex.getCause().getClass());
            assertEquals("Incorrect output document! Expected a correctly formatted JSON object.", ex.getMessage());
        }
    }

    /** See {@link RunCommandTest#run_ShouldFail_WhenUnknownCommand()} for a more detailed test. */
    @Test
    void parse_ShouldFail_WhenUnknownCommand() {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "foobla");

        // When:
        try {
            parser.parse(DUMB_QUERY, ADQLVersion.V2_0);
            fail("This test should have failed because the command to startValidation does not exist!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(RunParserException.class, ex.getClass());
            assertEquals("Failed to create the process!", ex.getMessage());
        }
    }

    /** See {@link RunCommandTest#run_ShouldFail_WhenTooLong()} */
    @Test
    void parse_ShouldFail_WhenTooLong() {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "test-resources/parsers/long_running.bash");

        // When:
        try {
            parser.parse(DUMB_QUERY, ADQLVersion.V2_0);
            fail("This test should have failed because the parsing is too long!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(RunParserException.class, ex.getClass());
            assertEquals("Time out! Cause: the command execution was too long (> 3 seconds)", ex.getMessage());
        }
    }

    @Test
    void parse_ShouldFail_WhenMissingSuccessFlag() {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "test-resources/parsers/missing_success_flag.bash");

        // When:
        try {
            parser.parse(DUMB_QUERY, ADQLVersion.V2_0);
            fail("This test should have failed because the property '"+ADQLParser.FIELD_SUCCESS+"' is missing!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(ADQLParseException.class, ex.getClass());
            assertEquals("Missing the JSON field '"+ADQLParser.FIELD_SUCCESS+"' (set to 'true' or 'false') in the parser output!", ex.getMessage());
        }
    }

    @Test
    void parse_ShouldFail_WhenMissingErrorFlag() {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "test-resources/parsers/missing_error_flag.bash");

        // When:
        try {
            parser.parse(DUMB_QUERY, ADQLVersion.V2_0);
            fail("This test should have failed because the property '"+ADQLParser.FIELD_ERROR+"' is missing!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(ADQLParseException.class, ex.getClass());
            assertEquals("Missing the JSON field '"+ADQLParser.FIELD_ERROR+"' in the parser output! An error message explaining why the ADQL query cannot be parsed MUST be provided.", ex.getMessage());
        }
    }

    @Test
    void parse_ShouldSucceed_WhenSuccessfulParsing() throws ADQLParseException {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "test-resources/parsers/successful_parsing.bash");

        // When:
        final ParsingResult result = parser.parse(DUMB_QUERY, ADQLVersion.V2_0);

        // Then:
        assertTrue(result.isSuccessful());
        assertTrue(result.getDuration().isPresent());
        assertTrue(result.getDuration().get() > 0);
        assertFalse(result.getErrorMessage().isPresent());

    }

    @Test
    void parse_ShouldSucceed_WhenFailedParsing() throws ADQLParseException {
        // Given:
        final ADQLParser parser = new ADQLParser("test", "test-resources/parsers/failed_parsing.bash");

        // When:
        final ParsingResult result = parser.parse(DUMB_QUERY, ADQLVersion.V2_0);

        // Then:
        assertFalse(result.isSuccessful());
        assertTrue(result.getDuration().isPresent());
        assertTrue(result.getDuration().get() > 0);
        assertTrue(result.getErrorMessage().isPresent());
        assertEquals("I failed because I wanted to!", result.getErrorMessage().get());

    }
}