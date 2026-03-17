package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.parser.validationset.ValidationSetParseException;
import cds.adql.validation.parser.validationset.xml.XMLValidationSetParser;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.ADQLValidationReport;

import java.io.InputStream;
import java.util.List;

/**
 * Validator tool for queries set and individual queries, whatever is their
 * format (XML or Java Object).
 *
 * <p>
 *     All parsers to run with this validator MUST be listed in the
 *     configuration file: {@value ParsersConfiguration#KEY_PARSERS} in
 *     {@value ParsersConfiguration#DEFAULT_PARSERS_CONFIGURATION_FILE}.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (03/2026)
 */
public class ADQLValidator {

    private final List<ADQLParser> parsers;

    /* ********************************************************************** */

    public ADQLValidator(final List<ADQLParser> parsers) {
        this.parsers    = parsers;
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
     * @throws ValidationSetParseException  When failed to parse
     *                                      the specified XML file.
     *
     * @see XMLValidationSetParser#checkXML(InputStream)
     */
    public void checkXML(final InputStream stream) throws ValidationSetParseException {
        final XMLValidationSetParser xmlParser = new XMLValidationSetParser();
        xmlParser.checkXML(stream);
    }


    /* *************************************************************************
     * VALIDATION METHODS
     */

    /**
     * Validate a valid XML document representing a complete validation set.
     *
     * <p>
     *     The parsed validation set will be validated thanks to
     *     {@link #validate(ValidationSet)}.
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
     * @return  A report for the validation of all queries available in the
     *          read {@link ValidationSet}.
     *
     * @throws ValidationSetParseException              When failed to parse
     *                                                  the specified XML file.
     *
     * @see #validate(ValidationSet)
     */
    public ADQLValidationReport validate(final InputStream stream, final String source) throws ValidationSetParseException
    {
        final ValidationSet tests = parseValidationSet(stream, source);
        return validate(tests);
    }

    private ValidationSet parseValidationSet(final InputStream stream, final String source) throws ValidationSetParseException
    {
        final XMLValidationSetParser parser = new XMLValidationSetParser();
        return parser.parse(stream, source);
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
     * @param set  Set of queries to validate.
     *
     * @return  A report for the validation of all queries available in the
     *          given {@link ValidationSet}.
     */
    public ADQLValidationReport validate(final ValidationSet set)
    {
        final ThreadPool threadPool = new ThreadPool(parsers.size());

        final ADQLValidationReport report = new ADQLValidationReport();

        final QuerySetValidation setValidation = new QuerySetValidation(set, threadPool, parsers, report);

        setValidation.startValidation();

        threadPool.stop();

        return report;
    }

}
