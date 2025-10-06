package cds.adql.validation.report;

import cds.adql.validation.parser.adql.ADQLVersion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Report of the validation for a single ADQL query and multiple parsers.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class QueryValidationReport implements Iterable<QueryValidationReportRecord> {

    private final List<QueryValidationReportRecord> records = new ArrayList<>();

    private int cntPassed     = 0;
    private boolean allPassed = true;

    /**
     * Report a successful parsing of a given ADQL query.
     *
     * @param parserID  (required) Designation of the used parser.
     * @param version   Version of the tested ADQL language.
     * @param duration  Parsing duration in milliseconds.
     */
    public void addSuccessfulValidation(final String parserID, final ADQLVersion version, final Long duration){
        cntPassed++;
        records.add(new QueryValidationReportRecord(parserID, version, true, duration, null));
    }

    /**
     * Report a failed parsing of a given ADQL query.
     *
     * @param parserID      (required) Designation of the used parser.
     * @param version       Version of the tested ADQL language.
     * @param duration      Parsing duration in milliseconds.
     * @param errorMessage  (optional) Message associated with this failure.
     */
    public void addFailedValidation(final String parserID, final ADQLVersion version, final Long duration, final String errorMessage){
        allPassed = false;
        records.add(new QueryValidationReportRecord(parserID, version, false, duration, errorMessage));
    }

    /**
     * Tell whether the query passed all available parsers.
     *
     * @return  <code>true</code> if all parsing were successful,
     *          <code>false</code> otherwise.
     */
    public boolean isPassed(){
        return allPassed;
    }

    /**
     * Get the number of all reported parsing results (successful or not).
     *
     * @return  Total number of parsing results.
     */
    public int size(){
        return records.size();
    }

    /**
     * Get the number of all successful parsing.
     *
     * @return  Number of successful parsing.
     */
    public int countPassed(){
        return cntPassed;
    }

    @Override
    public Iterator<QueryValidationReportRecord> iterator() {
        return records.iterator();
    }

}
