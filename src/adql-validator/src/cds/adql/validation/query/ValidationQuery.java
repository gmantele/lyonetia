package cds.adql.validation.query;

import cds.adql.validation.parser.adql.ADQLVersion;
import cds.adql.validation.parser.validationset.ValidationSetParser;

import java.util.*;

/**
 * Representation of a single Validation Test corresponding to an ADQL query to
 * parse and check.
 *
 * <p><b>IMPORTANT:</b>
 *     A {@link ValidationQuery} MUST have a unique UUID. If none is provided at
 *     initialization, one will be automatically created.
 * </p>
 *
 * <p>
 *     Though by default, there is no ADQL query set, one should immediately be
 *     created. Otherwise, this Validation Test is not useful.
 * </p>
 *
 * <p><b>IMPORTANT:</b>
 *     Two {@link ValidationQuery} instances are considered as equals if they
 *     have exactly the same UUID. No other piece of information (e.g. the ADQL
 *     query) is checked.
 * </p>
 *
 * <i>
 * <p><b>Notes:</b></p>
 * <ul>
 *     <li>
 *        Do not forget to set appropriately the flag about the expected test
 *        result: see {@link #setExpectedToBeValid(boolean)}.
 *     </li>
 *     <li>
 *        The highest possible target ADQL version should also be set if not
 *        corresponding to the latest in date: see {@link #adqlVersion} (by
 *        default: {@link ValidationSetParser#DEFAULT_ADQL_VERSION DEFAULT_ADQL_VERSION}.
 *     </li>
 * </ul>
 * </i>
 *
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class ValidationQuery {

    /** Unique ID of this test in an entire validation tests set. */
    private final UUID id;

    /** Human description of this test. */
    private String description = null;

    /** Definitions of all allowed User Defined Functions. */
    private final Set<UDF> functions = new LinkedHashSet<>(3);

    /** The ADQL query to test. */
    private String query = null;

    /** Indicate whether the query is expected to be valid or not. */
    private boolean isValid = false;

    /** Query's ADQL version target. */
    private ADQLVersion adqlVersion = ValidationSetParser.DEFAULT_ADQL_VERSION;

    /**
     * Create a {@link ValidationQuery} with a generated UUID.
     */
    public ValidationQuery(){
        this((UUID)null);
    }

    /**
     * Create a {@link ValidationQuery} with the given UUID.
     *
     * @param id    UUID of this validation test.
     *              <i>If NULL, one will be automatically generated.</i>
     */
    public ValidationQuery(final UUID id){
        this.id = (id == null) ? UUID.randomUUID() : id;
    }

    /**
     * Create a {@link ValidationQuery} with the given UUID.
     *
     * @param id    UUID of this validation test.
     *              <i>If NULL, one will be automatically generated.</i>
     */
    public ValidationQuery(final String id){
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID() : UUID.fromString(id);
    }

    public final UUID getId() {
        return id;
    }

    public Optional<String> getQuery() {
        return Optional.ofNullable(query);
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public void setDescription(final String description) {
        this.description = (description == null || description.isBlank()) ? null : description.trim();
    }

    public Iterator<UDF> getFunctions() {
        return functions.iterator();
    }

    public boolean isExpectedToBeValid() {
        return isValid;
    }

    public void setExpectedToBeValid(final boolean valid) {
        isValid = valid;
    }

    public ADQLVersion getADQLVersion() {
        return adqlVersion;
    }

    public void setADQLVersion(final ADQLVersion adqlVersion) {
        this.adqlVersion = (adqlVersion == null ? ValidationSetParser.DEFAULT_ADQL_VERSION : adqlVersion);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ValidationQuery testQuery = (ValidationQuery) o;
        return id.equals(testQuery.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
