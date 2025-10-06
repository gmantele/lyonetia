package cds.adql.validation.report;

import cds.adql.validation.parser.adql.ParsingResult;
import cds.adql.validation.parser.adql.exceptions.ADQLParseException;
import cds.adql.validation.query.ValidationQuery;

import java.util.*;

/**
 * Report for the validation of a set of ADQL queries.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class ADQLValidationReport {

    private final Map<UUID, QueryValidationReport> queryReports = new TreeMap<>();

    public void addSuccessfulValidation(final ValidationQuery query, final String parserName, final ParsingResult result) {
        final QueryValidationReport queryReport = getReportFor(query);
        queryReport.addSuccessfulValidation(parserName, query.getADQLVersion(), result.getDuration().orElse(null));
    }

    public void addFailedValidation(final ValidationQuery query, final String parserName, final ParsingResult result) {
        final QueryValidationReport queryReport = getReportFor(query);
        queryReport.addFailedValidation(parserName, query.getADQLVersion(), result.getDuration().orElse(null), result.getErrorMessage().orElse(null));
    }

    public void addGraveFailure(final ValidationQuery query, final String parserName, final ADQLParseException error) {
        final QueryValidationReport queryReport = getReportFor(query);
        queryReport.addFailedValidation(parserName, query.getADQLVersion(), null, error.getMessage());
    }

    protected QueryValidationReport getReportFor(final ValidationQuery query){
        return queryReports.computeIfAbsent(query.getId(), ignored -> new QueryValidationReport());
    }

    /**
     * Get the validation report for all queries.
     *
     * @return  List of reports (1 report for each validated query).
     */
    public Iterator<Map.Entry<UUID, QueryValidationReport>> getQueryReports(){
        return queryReports.entrySet().iterator();
    }
}
