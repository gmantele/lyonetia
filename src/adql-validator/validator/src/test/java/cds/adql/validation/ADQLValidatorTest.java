package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.parser.validationset.ValidationSetParseException;
import cds.adql.validation.parser.validationset.xml.XMLValidationSetParser;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static cds.adql.validation.ReportToolBox.checkCounts;
import static org.junit.jupiter.api.Assertions.*;

class ADQLValidatorTest {

    private static List<ADQLParser> parsers = new ArrayList<>();

    @BeforeAll
    static void beforeAll() throws IncorrectValidatorConfigurationException {
        parsers = ParsersConfiguration.getParsers("conditional-parser.properties");
    }

    /* *************************************************************************
     * GENERIC VALIDATION
     */

    @Test
    void validate_ShouldFail_WhenNoInputStream() {
        // Given:
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));
        final InputStream inputStream = null;
        final String source           = "Missing File";

        // When + Then:
        assertThrows(NullPointerException.class, () -> validator.validate(inputStream, source));
    }

    @Test
    void validate_ShouldFail_WhenNoValidationSet() {
        // Given:
        final ADQLValidator validator     = assertDoesNotThrow(() -> new ADQLValidator(parsers));
        final ValidationSet validationSet = null;

        // When + Then:
        assertThrows(NullPointerException.class, () -> validator.validate(validationSet));
    }

    @Test
    void validate_ShouldReturnAnEmptyReport_WhenEmptyValidationSet() {
        // Given:
        final ADQLValidator validator     = assertDoesNotThrow(() -> new ADQLValidator(parsers));
        final ValidationSet validationSet = new ValidationSet("empty");

        // When + Then:
        final ADQLValidationReport report = assertDoesNotThrow(() -> validator.validate(validationSet));
        assertFalse(report.getQueryReports().hasNext());
    }

    @Test
    void validate_ShouldSucceed_WhenAllValid() {
        // Given:
        final ValidationSet querySet = new ValidationSet("valid");
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));
        
        // Create 2 valid queries:
        // 1
        ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.setExpectedToBeValid(true);
        query.setQuery("SELECT x FROM y -- valid:true");
        querySet.add(query);
        // 2
        query = new ValidationQuery(UUID.randomUUID());
        query.setExpectedToBeValid(false);
        query.setQuery("SELECT everything -- valid:false");
        querySet.add(query);

        // When:
        final ADQLValidationReport report = assertDoesNotThrow(() -> validator.validate(querySet));

        // Then:
        checkCounts(report, 2, 0);
    }

    @Test
    void validate_ShouldReportOneFailureAndOneSuccess_WhenOneFailed()
    {
        // Given
        final ValidationSet querySet = new ValidationSet("one failed");
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // Create 2 valid queries:
        // 1
        ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.setExpectedToBeValid(false);
        query.setQuery("SELECT x FROM y -- valid:true");
        querySet.add(query);
        // 2
        query = new ValidationQuery(UUID.randomUUID());
        query.setExpectedToBeValid(false);
        query.setQuery("SELECT everything -- valid:false");
        querySet.add(query);

        // When:
        final ADQLValidationReport report = assertDoesNotThrow(() -> validator.validate(querySet));

        // Then:
        checkCounts(report, 1, 1);
    }

    @Test
    void validate_ShouldReportFailure_WhenEmptyQuery()
    {
        // Given
        final ValidationSet querySet = new ValidationSet("one failed");
        querySet.add(new ValidationQuery(UUID.randomUUID())); // add a query with query string
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // When:
        final ADQLValidationReport report = assertDoesNotThrow(() -> validator.validate(querySet));

        // Then:
        checkCounts(report, 0, 1);
        final QueryValidationReport queryReport = report.getQueryReports().next().getValue();
        final QueryValidationReportRecord reportRecord = queryReport.iterator().next();
        assertEquals("No ADQL query to parse!", reportRecord.getErrorMessage().orElseThrow());
    }

    /* *************************************************************************
     * XML VALIDATION
     */

    @Test
    void checkXML_ShouldFailed_WhenNoStreamProvided()
    {
        // Given:
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // When + Then:
        assertThrows(NullPointerException.class, ()->validator.checkXML(null),
                "Missing input validation set!");
    }

    /*
     * No need to test more checkXML(InputStream) here ; it is using the class
     * XMLValidationSetParser already very well tested.
     */

    @Test
    void validate_ShouldFailed_WhenNoValidationSet()
    {
        // Given:
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // When + Then:
        assertThrows(NullPointerException.class, ()->validator.validate(null),
                "Missing input validation set!");
    }

    @Test
    void checkXML_ShouldFail_WhenIncorrectXML() {
        // Given:
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // When:
        try {
            validator.checkXML(new ByteArrayInputStream("<hello>World</hello>".getBytes()));
            fail("The given XML document does not follow the expected XML schema! This line should have failed.");
        }
        // Then:
        catch(Exception ex){
            assertEquals(ValidationSetParseException.class, ex.getClass());
            assertEquals("org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 8; cvc-elt.1.a: Cannot find the declaration of element 'hello'.", ex.getMessage());
        }
    }

    @Test
    void checkXML_ShouldSucceed_WhenCorrectXMLWithQueries() {
        // Given:
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // When + Then:
        assertDoesNotThrow(() -> validator.checkXML(new ByteArrayInputStream("<queries><description>Some description for the entire set.</description><query uuid=\"ccd99070-4508-11e6-b60c-9d2c33f9b7a2\"><description>The simplest ADQL query.</description><adql valid=\"true\" version=\"adql-2.0\">select x from y</adql></query></queries>".getBytes())));
    }

    @Test
    void validate_ShouldSucceed_WhenCorrectXMLWithQueries() {
        // Given:
        final ADQLValidator validator = assertDoesNotThrow(() -> new ADQLValidator(parsers));

        // When:
        final ADQLValidationReport report = assertDoesNotThrow(() -> validator.validate(new ByteArrayInputStream("<queries><description>Some description for the entire set.</description><query uuid=\"ccd99070-4508-11e6-b60c-9d2c33f9b7a2\"><description>The simplest ADQL query.</description><adql valid=\"true\" version=\"adql-2.0\">select x from y -- valid:true</adql></query></queries>".getBytes()), "anonymous source"));

        // Then:
        checkCounts(report, 1, 0);
    }
}