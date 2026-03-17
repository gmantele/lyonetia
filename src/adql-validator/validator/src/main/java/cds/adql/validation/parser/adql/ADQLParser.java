package cds.adql.validation.parser.adql;

import cds.adql.validation.parser.adql.exceptions.ADQLParseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * ADQL parser overlay running the specified ADQL parser (which targets a
 * particular language and parser tool).
 *
 * <p>
 *  <b>This class is Thread-safe.</b> The function
 *  {@link #parse(String, ADQLVersion)} can be called from different threads in
 *  the same time with a different argument.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (05/2025)
 * @since 2.0
 */
public class ADQLParser {

    private static final String ARG_VERSION = "--version";

    static final String FIELD_SUCCESS  = "success";
    static final String FIELD_ERROR    = "error";

    private final String name;

    private final String parserToolCommand;

    public ADQLParser(final String name, final String parserCommand){
        this.name              = normalizeParserName(name);
        this.parserToolCommand = Objects.requireNonNull(parserCommand);
    }

    private static String normalizeParserName(final String name) throws NullPointerException {
        if (name == null || name.isBlank())
            throw new NullPointerException("Missing parser name!");
        else
            return name.trim();
    }

    public final String getName() {
        return name;
    }

    /**
     * Parse the given ADQL query.
     *
     * <p>
     *  Nothing is returned in case of success. However, in case of failure,
     *  an {@link ADQLParseException} is immediately thrown explaining as much
     *  as possible why the ADQL could not be parsed.
     * </p>
     *
     * @param query     The ADQL query to parse.
     * @param version   ADQL version to startValidation the query with.
     *
     * @throws ADQLParseException   In case of failure. This exception should
     *                              provide a human error message explaining why
     *                              the parsing failed.
     */
    public ParsingResult parse(final String query, final ADQLVersion version) throws ADQLParseException {
        final CommandOutput commandOutput = runParsingCommand(Objects.requireNonNull(query),
                                                              Objects.requireNonNull(version).toString());
        return convertIntoParsingResult(commandOutput);
    }

    private CommandOutput runParsingCommand(final String query, final String version) throws ADQLParseException {
        final RunCommand runCmd = new RunCommand(parserToolCommand,
                                                 ARG_VERSION, version, query);
        return runCmd.run();
    }

    private ParsingResult convertIntoParsingResult(final CommandOutput commandOutput) throws ADQLParseException {
        try {
            final JSONObject jsonDoc = new JSONObject(commandOutput.output);
            return convertIntoParsingResult(jsonDoc, commandOutput.duration);
        }
        catch(JSONException je){
            throw new ADQLParseException("Incorrect output document! Expected a correctly formatted JSON object.", je);
        }
    }

    private ParsingResult convertIntoParsingResult(final JSONObject jsonDoc, final long duration) throws ADQLParseException {
        final ParsingResult parsingResult = new ParsingResult();

        updateParsingStatus(jsonDoc, parsingResult);
        parsingResult.setDuration(duration);

        if (!parsingResult.isSuccessful())
            updateParsingError(jsonDoc, parsingResult);

        return parsingResult;
    }

    private void updateParsingStatus(final JSONObject jsonDoc, final ParsingResult parsingResult) throws ADQLParseException {
        if (jsonDoc.has(FIELD_SUCCESS))
            parsingResult.setSuccessful(jsonDoc.getBoolean(FIELD_SUCCESS));
        else
            throw new ADQLParseException("Missing the JSON field '"+FIELD_SUCCESS+"' (set to 'true' or 'false') in the parser output!");
    }

    private void updateParsingError(final JSONObject jsonDoc, final ParsingResult parsingResult) throws ADQLParseException {
        if (jsonDoc.has(FIELD_ERROR))
            parsingResult.setErrorMessage(jsonDoc.getString(FIELD_ERROR));
        else
            throw new ADQLParseException("Missing the JSON field '"+FIELD_ERROR+"' in the parser output! An error message explaining why the ADQL query cannot be parsed MUST be provided.");
    }

}
