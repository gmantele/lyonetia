package cds.adql.validation.report;

import cds.adql.validation.parser.adql.ADQLVersion;
import cds.adql.validation.parser.validationset.ValidationSetParser;

import java.util.Objects;
import java.util.Optional;

/**
 * Representation of a single record for a {@link QueryValidationReport}.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (05/2025)
 *
 * @see QueryValidationReport
 */
public class QueryValidationReportRecord {

    private final String parserID;
    private final ADQLVersion version;

    private final boolean validationPassed;
    private final Long duration;

    private final String errorMessage;

    /**
     * Create record for a query validation report.
     *
     * @param parserID          ID of the used parser. This is required.
     * @param version           Version of the tested ADQL language. Set to
     *                          {@link ValidationSetParser#DEFAULT_ADQL_VERSION}
     *                          when <code>null</code>.
     * @param validationPassed  <code>true</code> if the query passed the
     *                          validation,
     *                          <code>false</code> otherwise.
     * @param duration          Parsing execution duration (in milliseconds),
     *                          or <code>null</code> in case of failure.
     * @param errorMessage      Error message in case of failure,
     *                          or <code>null</code> if successful.
     */
    public QueryValidationReportRecord(final String parserID, final ADQLVersion version, final boolean validationPassed, final Long duration, final String errorMessage) {
        this.parserID         = Objects.requireNonNull(parserID, "ID of the used parser is required!");
        this.version          = (version == null ? ValidationSetParser.DEFAULT_ADQL_VERSION : version);
        this.validationPassed = validationPassed;
        this.duration         = duration;
        this.errorMessage     = (errorMessage == null || errorMessage.isBlank()) ? null : errorMessage.trim();
    }

    public final String getParserID() {
        return parserID;
    }

    public final ADQLVersion getVersion() { return version; }

    public final boolean isValidationPassed() {
        return validationPassed;
    }

    /**
     * Get the parsing execution duration (in milliseconds).
     *
     * @return  Parsing duration in milliseconds.
     */
    public final Optional<Long> getDuration(){
        return Optional.ofNullable(duration);
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
