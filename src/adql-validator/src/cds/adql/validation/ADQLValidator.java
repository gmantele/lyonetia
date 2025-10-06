package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.parser.adql.exceptions.ADQLParseException;
import cds.adql.validation.parser.validationset.ValidationSetParseException;
import cds.adql.validation.parser.validationset.xml.XMLValidationSetParser;
import cds.adql.validation.query.UDF;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Validator tool for queries set and individual queries, whatever is their
 * format (XML or Java Object).
 *
 * <p>
 *     All parsers to run with this validator MUST be listed in the
 *     configuration file: {@value ParsersConfiguration#PARSERS_CONFIGURATION_FILE}.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class ADQLValidator {

    static final Logger LOGGER = Logger.getLogger(ADQLValidator.class.getName());

    private final List<ADQLParser> parsers;

    /* ********************************************************************** */

    public ADQLValidator(final ExecutorService threadPool) throws IncorrectValidatorConfigurationException {
        parsers   = ParsersConfiguration.getParsers(threadPool);
    }


    /* *************************************************************************
     * XML VALIDATION
     */

    /**
     *
     * Check that the document provided by the given stream is a valid XML
     * document, as expected by this {@link ADQLValidator}.
     *
     * @param stream    Stream toward the document to parse.
     *
     * @return boolean  <code>true</code> if valid XML document,
     *                  <code>false</code> otherwise (the error has already been
     *                  reported in registered listeners).
     *
     * @see XMLValidationSetParser#checkXML(InputStream)
     */
    public boolean checkXML(final InputStream stream) {
        try {
            (new XMLValidationSetParser()).checkXML(stream);
            return true;
        }catch(ValidationSetParseException pe){
            //publishError(new ValidationException("Incorrect XML syntax! Cause: "+pe.getMessage(), pe));
            return false;
        }
    }


    /* *************************************************************************
     * VALIDATION METHODS
     */

    /**
     * Validate a valid XML document representing a complete validation set.
     *
     * <p>
     *     The parsed validation set will be validated thanks to
     *     {@link #validate(ValidationSet, String)}.
     * </p>
     *
     * <p>
     *     Validation status, progress, success and failure are all reported to
     *     all registered listeners.
     * </p>
     *
     * @param stream    Stream toward the XML validation set.
     * @param source    Human information about where the document comes from
     *                  (example: <code>File /foo/bar.xml</code>). It is purely
     *                  informal. It aims to improve the documentation of the
     *                  validation process.
     *
     * @return  <code>true</code> if all tests passed inside the validation set,
     *          <code>false</code> in case of error or if at least one test
     *          failed.
     *
     * @see XMLValidationSetParser#parse(InputStream)
     * @see #validate(ValidationSet, String)
     */
    public boolean validate(final InputStream stream, final String source){
        // Parse the file:
        ValidationSet tests;
        try{
            // Parse the XML document and transform it into a tests set:
            tests = (new XMLValidationSetParser()).parse(stream);
            // Validate the tests set:
            if (tests != null)
                return validate(tests, source);
                // Or report an error:
            else {
                //publishError(new ValidationException("No validation set provided!"));
                return false;
            }
        }
        catch (ValidationSetParseException e) {
            //publishError(new ValidationException("XML document parsing failed! Cause: "+e.getMessage(), e));
            return false;
        }
    }

    /**
     * Validate a single ADQL query.
     *
     * <p><i><b>Note:</b>
     *   NULL or an empty query string will make this function immediately
     *   return <code>false</code>.
     * </i></p>
     *
     * @param query     The query to validate.
     *
     * @return  <code>true</code> if this query passed the validation test,
     *          <code>false</code> otherwise.
     */
    public boolean validate(final ValidationQuery query){
        return validate(query, null);
    }

    /**
     * Validate all queries of the given validation set.
     *
     * <p>
     *     This function does NOT stop the validation at the first failed query.
     *     All queries are tested so that the final report can be as complete as
     *     possible.
     * </p>
     *
     * @param set       Set of queries to validate.
     * @param source    Human information about where the document comes from
     *                  (example: <code>File /foo/bar.xml</code>). It is purely
     *                  informal. It aims to improve the documentation of the
     *                  validation process.
     *
     * @return  <code>true</code> if all queries of the validation set passed,
     *          <code>false</code> in case of error, NULL or if one query failed.
     */
    public boolean validate(final ValidationSet set, final String source){
        // Nothing to validate if NULL:
        if (set == null)
            return false;

        // Validate all queries:
        boolean allValid = true;
        for(ValidationQuery query : set.queries) {
            /* Always try to validate all queries even if allValid is false.
             * That way, all errors of all queries are reported. */
            allValid = validate(query, set.functions) && allValid;
        }

        return allValid;
    }

    /**
     * Validate a single ADQL query.
     *
     * <p><i><b>Note:</b>
     *   NULL or an empty query string will make this function immediately
     *   return <code>false</code>.
     * </i></p>
     *
     * @param query     The query to validate.
     * @param functions Global UDFs to support while validating the query.
     *
     * @return  <code>true</code> if this query passed the validation test,
     *          <code>false</code> otherwise.
     */
    public boolean validate(final ValidationQuery query, final Set<UDF> functions){
        // Nothing to validate if NULL:
        if (query == null
                || query.query == null
                || query.query.trim().isEmpty())
        {
            return false;
        }

        boolean valid;
        String err = null;

        for(ADQLParser parser : parsers) {

            // TODO See how to detect UDF
            /*// Declare all UDFs to support:
            if (functions != null) {
                for (UDF u : functions)
                    parser.getSupportedFeatures().support(u.getFeature());
            }
            for (UDF u : query.functions)
                parser.getSupportedFeatures().support(u.getFeature());*/

            // Parse the ADQL query:
            try {
                parser.parse(query.query, query.adqlVersion); // TODO Ensure query.adqlVersion is never NULL (make this property private and add getter/setter)
                valid = true;
            } catch (ADQLParseException pe) {
                valid = false;
                err = pe.getMessage();
            }

            // Report the validation result:
            final boolean success = (valid == query.isValid);
        }

        return success; // TODO Collect all success status (and return them? or combine them?)
    }

}
