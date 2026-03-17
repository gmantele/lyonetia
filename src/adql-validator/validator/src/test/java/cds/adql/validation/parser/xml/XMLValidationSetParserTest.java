package cds.adql.validation.parser.xml;

import cds.adql.validation.parser.ParseException;
import cds.adql.validation.parser.ValidationSetParser;
import cds.adql.validation.parser.adql.ADQLVersion;
import cds.adql.validation.parser.validationset.ValidationSetParseException;
import cds.adql.validation.parser.validationset.xml.XMLValidationSetParser;
import cds.adql.validation.query.UDF;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class XMLValidationSetParserTest {

    @Test
    void checkXML_Null() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        assertThrows(NullPointerException.class,
                     ()->parser.checkXML(null),
                     "Missing input validation set!");
    }

    @Test
    void checkXML_Valid() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        assertDoesNotThrow(()->parser.checkXML(new ByteArrayInputStream("<queries><description>Some description for the entire set.</description><query uuid=\"ccd99070-4508-11e6-b60c-9d2c33f9b7a2\"><description>Simple geometry predicate</description><adql valid=\"true\" version=\"adql-2.0\">select x from y where Point(NULL, 2, 3)=x</adql></query></queries>".getBytes())));
    }

    @Test
    void checkXML_Invalid() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.checkXML(new ByteArrayInputStream("<hello>World</hello>".getBytes())));
        assertEquals(SAXParseException.class, t.getCause().getClass());
        assertEquals("cvc-elt.1.a: Cannot find the declaration of element 'hello'.", t.getCause().getMessage());
    }

    @Test
    void parse_Null() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(NullPointerException.class, ()->parser.parse(null, "Null"));
        assertEquals("Missing input validation set!", t.getMessage());
    }

    @Test
    void parse_Empty() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.parse(new ByteArrayInputStream("".getBytes()), "Empty"));
        assertNull(t.getCause());
        assertEquals("Unsupported ADQL validation set format! Expected: XML document.", t.getMessage());
    }

    @Test
    void parse_Not_XML() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.parse(new ByteArrayInputStream("Hello World!".getBytes()), "Not XML"));
        assertNull(t.getCause());
        assertEquals("Unsupported ADQL validation set format! Expected: XML document.", t.getMessage());
    }

    @Test
    void parse_Incorrect_First_Characters() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.parse(new ByteArrayInputStream(".<queries></queries>".getBytes()), "Bad 1st char"));
        assertNull(t.getCause());
        assertEquals("Unsupported ADQL validation set format! Expected: XML document.", t.getMessage());
    }

    @Test
    void parse_Unsupported_XML() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.parse(new ByteArrayInputStream("<hello>World!</hello>".getBytes()), "Unsupported XML"));
        assertEquals(SAXParseException.class, t.getCause().getClass());
        assertEquals("Unsupported XML root tag: <hello>! Expected: <queries>.", t.getCause().getMessage());
    }

    @Test
    void parse_Incorrect_XML() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.parse(new ByteArrayInputStream("<queries>".getBytes()), "Incorrect XML"));
        assertEquals(SAXParseException.class, t.getCause().getClass());
        assertEquals("XML document structures must start and end within the same entity.", t.getCause().getMessage());
    }

    @Test
    void parse_Inner_Queries() {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final Throwable t = assertThrows(ValidationSetParseException.class, ()->parser.parse(new ByteArrayInputStream("<queries><queries></queries></queries>".getBytes()), "Inner Queries"));
        assertEquals(SAXParseException.class, t.getCause().getClass());
        assertEquals("Inner queries suites not supported!", t.getCause().getMessage());
    }

    @Test
    void parse_Unsupported_Tags() throws ValidationSetParseException {
        final String XML = "<queries>" + System.lineSeparator() +
                           "<contact><description>Description of what??</description></contact>" + System.lineSeparator() +
                           "<foo>Unsupported element!</foo>" + System.lineSeparator() +
                           "<name>Unsupported name in here!</name>" + System.lineSeparator() +
                           "<url>http://unsupported/url/in/here</url>" + System.lineSeparator() +
                           "<query><adql>SELECT something FROM somewhere</adql></query>" + System.lineSeparator() +
                           "</queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the XML:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet set = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Unsupported tags");
        assertEquals(1, set.countQueries());
        final ValidationQuery query = set.getQueries().next();

        // Ensure expected warnings have been logged:
        final LogRecord[] expected = new LogRecord[]{
                new LogRecord(Level.WARNING, "[l.2, c.23] Unexpected <description> element!"),
                new LogRecord(Level.WARNING, "[l.3, c.6] Unexpected <foo> element!"),
                new LogRecord(Level.WARNING, "[l.4, c.7] Unexpected <name> element!"),
                new LogRecord(Level.WARNING, "[l.5, c.6] Unexpected <url> element!"),
                new LogRecord(Level.INFO, "[l.6, c.8] Omitted value for the attribute 'uuid'. Automatically generated and set to: '"+query.getId()+"'."),
                new LogRecord(Level.INFO, "[l.6, c.14] Omitted value for the attribute 'valid'. Set by default to: 'false'."),
                new LogRecord(Level.INFO, "[l.6, c.14] Omitted value for the attribute 'version'. Set by default to: '2.1'.")
        };
        assertLogs(expected, logHandler);
    }

    @Test
    void parse_Incorrect_Value_Attribute() throws ValidationSetParseException {
        final String XML = "<queries><query uuid=\""+UUID.randomUUID()+"\">" + System.lineSeparator() +
                "<adql valid=\"foo\" version=\"2.1\">SELECT foo FROM bar</adql>" + System.lineSeparator() +
                "</query></queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Incorrect attribute value");

        // Exactly one query:
        assertNotNull(validationSet);
        assertEquals(1, validationSet.countQueries());
        final ValidationQuery validationQuery = validationSet.getQueries().next();
        assertNotNull(validationQuery);

        // Check the logs:
        final LogRecord[] expectedLogs = new LogRecord[]{
                new LogRecord(Level.WARNING, "[l.2, c.33] Unsupported value for the attribute 'valid': 'foo'! Set by default to: 'false'.")
        };
        assertLogs(expectedLogs, logHandler);
    }

    @Test
    void parse_Incorrect_Version_Attribute() throws ValidationSetParseException {
        final String XML = "<queries><query uuid=\""+UUID.randomUUID()+"\">" + System.lineSeparator() +
                "<adql valid=\"true\" version=\"foo\">SELECT foo FROM bar</adql>" + System.lineSeparator() +
                "</query></queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Incorrect version attribute");

        // Exactly one query:
        assertNotNull(validationSet);
        assertEquals(1, validationSet.countQueries());
        final ValidationQuery validationQuery = validationSet.getQueries().next();
        assertNotNull(validationQuery);

        // Check the logs:
        final LogRecord[] expectedLogs = new LogRecord[]{
                new LogRecord(Level.WARNING, "[l.2, c.34] Unsupported value for the attribute 'version': 'foo'! Set by default to: '"+ValidationSetParser.DEFAULT_ADQL_VERSION+"'.")
        };
        assertLogs(expectedLogs, logHandler);
    }

    @Test
    void parse_Incorrect_Contact_URL() throws ValidationSetParseException {
        final String CONTACT_NAME = "M. Blabla";
        final String BAD_URL = "blabla_but_no_url";
        final String XML = "<queries>" + System.lineSeparator() +
                "<contact><name>"+CONTACT_NAME+"</name><url>"+BAD_URL+"</url></contact>" + System.lineSeparator() +
                "<query uuid=\""+UUID.randomUUID()+"\">" + System.lineSeparator() +
                "<adql valid=\"true\" version=\"2.1\">SELECT foo FROM bar</adql>" + System.lineSeparator() +
                "</query>" + System.lineSeparator() +
                "</queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Incorrect contact URL");

        // Existing contact:
        assertTrue(validationSet.getContact().isPresent());
        assertEquals(CONTACT_NAME, validationSet.getContact().get().name);
        assertNull(validationSet.getContact().get().url);

        // Check the logs:
        final LogRecord[] expectedLogs = new LogRecord[]{
                new LogRecord(Level.WARNING, "[l.2, c.60] Malformed contact's URL: '"+BAD_URL+"'! Cause: no protocol: blabla_but_no_url")
        };
        assertLogs(expectedLogs, logHandler);
    }

    @Test
    void parse_Incorrect_Publisher_URL() throws ValidationSetParseException {
        final String PUBLISHER_NAME = "M. Blabla";
        final String BAD_URL = "blabla_but_no_url";
        final String XML = "<queries>" + System.lineSeparator() +
                "<publisher><name>"+PUBLISHER_NAME+"</name><url>"+BAD_URL+"</url></publisher>" + System.lineSeparator() +
                "<query uuid=\""+UUID.randomUUID()+"\">" + System.lineSeparator() +
                "<adql valid=\"true\" version=\"2.1\">SELECT foo FROM bar</adql>" + System.lineSeparator() +
                "</query>" + System.lineSeparator() +
                "</queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Incorrect publisher URL");

        // Existing publisher:
        assertTrue(validationSet.getPublisher().isPresent());
        assertEquals(PUBLISHER_NAME, validationSet.getPublisher().get().name);
        assertNull(validationSet.getPublisher().get().url);

        // Check the logs:
        final LogRecord[] expectedLogs = new LogRecord[]{
                new LogRecord(Level.WARNING, "[l.2, c.62] Malformed publisher's URL: '"+BAD_URL+"'! Cause: no protocol: blabla_but_no_url")
        };
        assertLogs(expectedLogs, logHandler);
    }

    @Test
    void parse_Empty_Contact() throws ValidationSetParseException {
        final String XML = "<queries>" + System.lineSeparator() +
                "<contact></contact>" + System.lineSeparator() +
                "<query uuid=\""+UUID.randomUUID()+"\">" + System.lineSeparator() +
                "<adql valid=\"true\" version=\"2.1\">SELECT foo FROM bar</adql>" + System.lineSeparator() +
                "</query>" + System.lineSeparator() +
                "</queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Empty contact");

        // No contact:
        assertFalse(validationSet.getContact().isPresent());
    }

    @Test
    void parse_Empty_Publisher() throws ValidationSetParseException {
        final String XML = "<queries>" + System.lineSeparator() +
                "<publisher></publisher>" + System.lineSeparator() +
                "<query uuid=\""+UUID.randomUUID()+"\">" + System.lineSeparator() +
                "<adql valid=\"true\" version=\"2.1\">SELECT foo FROM bar</adql>" + System.lineSeparator() +
                "</query>" + System.lineSeparator() +
                "</queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Empty publisher");

        // No publisher:
        assertFalse(validationSet.getPublisher().isPresent());
    }

    @Test
    void parse_No_ADQL_And_So_No_Query() throws ValidationSetParseException {
        final String ID = UUID.randomUUID().toString();
        final String XML = "<queries><query uuid=\""+ID+"\"></query></queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "No query");

        // Exactly one query:
        assertNotNull(validationSet);
        assertEquals(0, validationSet.countQueries());

        // Check the logs:
        final LogRecord[] expectedLogs = new LogRecord[]{
                new LogRecord(Level.WARNING, "[l.1, c.69] Missing ADQL query for the validation query '"+ID+"'!"),
                new LogRecord(Level.INFO, "[l.1, c.79] No query to validate in this validation set!")
        };
        assertLogs(expectedLogs, logHandler);
    }

    @Test
    void parse_Minimal_Query() throws ValidationSetParseException {
        final String ADQL = "select x from y where Point(NULL, 2, 3)=x";
        final String QUERY = "<queries><query>" + System.lineSeparator() +
                "<adql>"+ADQL+"</adql>" + System.lineSeparator() +
                "</query></queries>";

        // Configure the Log handler:
        final TestLogHandler logHandler = setLogHandler();

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(QUERY.getBytes()), "Min query");

        // Exactly one query:
        assertNotNull(validationSet);
        assertEquals(1, validationSet.countQueries());

        final ValidationQuery validationQuery = validationSet.getQueries().next();
        assertNotNull(validationQuery);

        // Expected query should be exactly the given one:
        assertEquals(ADQL, validationQuery.getQuery().orElse("-"));

        // If none specified, default ADQL version:
        assertEquals(ValidationSetParser.DEFAULT_ADQL_VERSION, validationQuery.getADQLVersion());

        // No default description:
        assertFalse(validationQuery.getDescription().isPresent());

        // By default, the query is expected to be invalid:
        assertFalse(validationQuery.isExpectedToBeValid());

        // Check the logs:
        final LogRecord[] expectedLogs = new LogRecord[]{
                new LogRecord(Level.INFO, "[l.1, c.17] Omitted value for the attribute 'uuid'. Automatically generated and set to: '"+validationQuery.getId()+"'."),
                new LogRecord(Level.INFO, "[l.2, c.7] Omitted value for the attribute 'valid'. Set by default to: 'false'."),
                new LogRecord(Level.INFO, "[l.2, c.7] Omitted value for the attribute 'version'. Set by default to: '"+ValidationSetParser.DEFAULT_ADQL_VERSION+"'.")
        };
        assertLogs(expectedLogs, logHandler);

    }

    @Test
    void parse_Complete_Query() throws ParseException, ValidationSetParseException {
        final UUID ID = UUID.fromString("ccd99070-4508-11e6-b60c-9d2c33f9b7a2");
        final String ADQL = "select x from y where Point(NULL, 2, 3)=x";
        final String DESCRIPTION = "Simple geometry predicate";
        final String QUERY = "<queries><description>Some description for the entire set.</description>" +
                             "<query uuid=\""+ID+"\">" +
                             "<description>"+DESCRIPTION+"</description>" +
                             "<adql valid=\"true\" version=\"adql-2.0\">"+ADQL+"</adql>" +
                             "</query></queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(QUERY.getBytes()), "Complete query");

        // Exactly one query:
        assertNotNull(validationSet);
        assertEquals(1, validationSet.countQueries());

        final ValidationQuery validationQuery = validationSet.getQueries().next();
        assertNotNull(validationQuery);

        System.out.println(QUERY);

        // All fields are filled exactly with what is provided:
        assertEquals(ID, validationQuery.getId());
        assertEquals(ADQL, validationQuery.getQuery().orElse("-"));
        assertEquals(ADQLVersion.V2_0, validationQuery.getADQLVersion());
        assertEquals(DESCRIPTION, validationQuery.getDescription().orElse("-"));
        assertTrue(validationQuery.isExpectedToBeValid());
    }

    @Test
    void parse_Multiline_Query() throws ValidationSetParseException {
        final String ADQL = "select x from y where Point(NULL, 2, 3)=x\n\t\t\t\tAND mag BETWEEN 1 AND 10\n";
        final String QUERY = "<queries><query>" +
                "<adql valid=\"true\">"+ADQL+"</adql>" +
                "</query></queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(QUERY.getBytes()), "Multiline query");

        // Exactly one query:
        assertNotNull(validationSet);
        assertEquals(1, validationSet.countQueries());

        // Query equality expected:
        final ValidationQuery validationQuery = validationSet.getQueries().next();
        assertNotNull(validationQuery);
        assertEquals(ADQL.trim(), validationQuery.getQuery().orElse("-"));
    }

    @Test
    void parse_UDF_in_Queries() throws ValidationSetParseException {
        final String UDF_FORM = "blabla()";
        final String XML = "<queries><functions><function><form>"+UDF_FORM+"</form><description>Blabla function.</description></function></functions></queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "UDF in queries");

        // Check there is exactly one UDF:
        assertEquals(1, validationSet.countFunctions());

        // Check the function form:
        assertEquals(UDF_FORM, validationSet.getFunctions().next().getForm());
    }

    @Test
    void parse_UDF_in_A_query() throws ValidationSetParseException {
        final String UDF_FORM = "blabla()";
        final String XML = "<queries><query uuid=\"ee45b3bd-6902-4f77-bca1-3212f76e31bb\"><functions><function><form>"+UDF_FORM+"</form><description>Blabla function.</description></function></functions><adql valid=\"true\" version=\"2.1\">SELECT * FROM atable</adql></query></queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "UDF in a query");

        // Check there is exactly one query:
        assertEquals(1, validationSet.countQueries());

        // Check this query has exactly one UDF:
        final ValidationQuery QUERY = validationSet.getQueries().next();
        final Iterator<UDF> itFunctions = QUERY.getFunctions();
        assertTrue(itFunctions.hasNext());
        final UDF udf = itFunctions.next();
        assertFalse(itFunctions.hasNext());

        // Check the function form:
        assertEquals(UDF_FORM, udf.getForm());
    }

    @Test
    void parse_Duplicated_UDF() throws ValidationSetParseException {
        final String UDF_FORM = "blabla()";
        final String UDF_DEF = "<function><form>"+UDF_FORM+"</form><description>Blabla function.</description></function>";
        final String XML = "<queries><functions>"+UDF_DEF+UDF_DEF+"</functions></queries>";

        // Parse the validation set:
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        final ValidationSet validationSet = parser.parse(new ByteArrayInputStream(XML.getBytes()), "Duplicated UDF");

        // Check there is exactly one UDF:
        assertEquals(1, validationSet.countFunctions());

        // Check the function form:
        assertEquals(UDF_FORM, validationSet.getFunctions().next().getForm());
    }


    /* **********************************************************************
       *                         TOOL FUNCTIONS                             *
       ********************************************************************** */

    private TestLogHandler setLogHandler(){
        final TestLogHandler logHandler = new TestLogHandler();
        logHandler.setLevel(Level.ALL);

        final Logger parserLogger = Logger.getLogger(XMLValidationSetParser.class.getName());
        parserLogger.setUseParentHandlers(false);
        parserLogger.addHandler(logHandler);
        parserLogger.setLevel(Level.ALL);

        return logHandler;
    }

    private void assertLogs(final LogRecord[] expected, final TestLogHandler logHandler){
        // Test the number of elements first (so that avoiding ArrayIndexOutOfBoundsException):
        assertEquals(expected.length, logHandler.countLogs());

        // Test each log record:
        int indLog = 0;
        for(LogRecord log : logHandler){
            assertEquals(expected[indLog].getLevel(), log.getLevel());
            assertEquals(expected[indLog].getMessage(), log.getMessage());
            indLog++;
        }
    }
}