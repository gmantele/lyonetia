package cds.adql.validation.parser.adql;

import java.util.Optional;

/**
 * Parsing execution result.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 05/2025
 */
public class ParsingResult {

    private boolean isSuccessful = false;
    private String errorMessage = null;
    private Long duration = null;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Get the parsing duration (in milliseconds).
     *
     * @return  Parsing duration in milliseconds.
     */
    public Optional<Long> getDuration() {
        return Optional.ofNullable(duration);
    }

    public void setSuccessful(final boolean successful) {
        isSuccessful = successful;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Set the parsing duration (in milliseconds).
     *
     * @param duration  Duration in milliseconds.
     */
    public void setDuration(final Long duration) {
        this.duration = duration;
    }

}
