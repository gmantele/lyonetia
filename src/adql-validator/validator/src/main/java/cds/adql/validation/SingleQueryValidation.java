package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.parser.adql.ParsingResult;
import cds.adql.validation.parser.adql.exceptions.ADQLParseException;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.report.ADQLValidationReport;

import java.util.Objects;

/**
 * Validator for a single ADQL query with a single given parser.
 *
 * <p>
 *  Once the query is parsed, its result is analyzed in order to know whether
 *  it passed the validation. The given report is consequently updated.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class SingleQueryValidation implements Runnable {

    private final ADQLParser           parser;
    private final ValidationQuery      query;
    private final ADQLValidationReport report;

    /**
     * Configure the validation operation of a single query.
     *
     * @param query     The query to validate and what is the expected result.
     * @param parser    The parser to use.
     * @param report    The validation report to update.
     */
    public SingleQueryValidation(final ValidationQuery query, final ADQLParser parser, final ADQLValidationReport report) {
        this.parser = Objects.requireNonNull(parser);
        this.query  = Objects.requireNonNull(query);
        this.report = Objects.requireNonNull(report);
    }

    @Override
    public void run() {
        try {
            final ParsingResult result = parseQuery();
            updateReport(result);
        }
        catch(ADQLParseException ape){
            report.addGraveFailure(query, parser.getName(), ape);
        }
    }

    protected ParsingResult parseQuery() throws ADQLParseException {
        final String adqlQuery = query.getQuery()
                                      .orElseThrow(()-> new ADQLParseException("No ADQL query to parse!"));
        // TODO loop on all ADQL version to test with!
        return parser.parse(adqlQuery, query.getADQLVersion());
    }

    protected void updateReport(final ParsingResult result){
        if (result.isSuccessful() == query.isExpectedToBeValid())
            report.addSuccessfulValidation(query, parser.getName(), result);
        else
            report.addFailedValidation(query, parser.getName(), result);
    }

}
