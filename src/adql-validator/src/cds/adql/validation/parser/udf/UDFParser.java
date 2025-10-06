package cds.adql.validation.parser.udf;

import cds.adql.validation.query.UDF;

import java.util.regex.Pattern;

/**
 * Parse the string definition of a User Defined Function (UDF).
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 */
public class UDFParser {

    /** Regular expression for what should be a function or parameter name - a
     * regular identifier. */
    protected static final String REGEXP_REGULAR_IDENTIFIER = "[a-zA-Z]+\\w*";

    /** Rough regular expression for a function return type or a parameter type.
     * The exact type is not checked here ; just the type name syntax is tested,
     * not its value. This regular expression allows a type to have exactly one
     * parameter (which is generally the length of a character or binary
     * string. */
    protected static final String REGEXP_TYPE = "([a-zA-Z_]+[ 0-9a-zA-Z_]*)(\\(\\s*(\\d+)\\s*\\))?";

    /** Rough regular expression for a function parameters' list. */
    protected static final String REGEXP_FUNCTION_PARAMETERS = "\\s*[^,]+\\s*(,\\s*[^,]+\\s*)*";

    /** Rough regular expression for a function parameter: a name
     * (see {@link #REGEXP_REGULAR_IDENTIFIER}) and a type (see
     * {@link #REGEXP_TYPE}). */
    protected static final String REGEXP_FUNCTION_PARAMETER = "\\s*(" + REGEXP_REGULAR_IDENTIFIER + ")\\s+" + REGEXP_TYPE + "\\s*";

    /** Rough regular expression for a whole function definition. */
    protected static final String REGEXP_FUNCTION_DEFINITION = "\\s*(" + REGEXP_REGULAR_IDENTIFIER + ")\\s*\\(([a-zA-Z0-9_,() \r\n\t]*)\\)(\\s*->\\s*(" + REGEXP_TYPE + "))?\\s*";

    /** Pattern of a function definition. This object has been compiled with
     * {@link #REGEXP_FUNCTION_DEFINITION}. */
    protected static final Pattern PATTERN_FUNCTION = Pattern.compile(REGEXP_FUNCTION_DEFINITION);

    /** Pattern of a single parameter definition. This object has been compiled
     * with {@link #REGEXP_FUNCTION_PARAMETER}. */
    protected static final Pattern PATTERN_PARAMETER = Pattern.compile(REGEXP_FUNCTION_PARAMETER);

    private UDFParser(){ /* Forbid class instances. */ }

    public static UDF parse(String strDefinition) throws UDFParseException {
        strDefinition = strDefinition.trim().replaceAll("[\n\t\r]", " ").replaceAll(" +", " ");
        // TODO

        return null;
    }

}
