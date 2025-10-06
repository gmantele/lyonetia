package cds.adql.validation.parser.adql.exceptions;

import cds.adql.validation.parser.adql.ADQLParser;

/**
 * Exception thrown while parsing an ADQL query.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (04/2025)
 * @see ADQLParser
 */
public class ADQLParseException extends Exception {

    public ADQLParseException() {}

    public ADQLParseException(String s) {
        super(s);
    }

    public ADQLParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ADQLParseException(Throwable throwable) {
        super(throwable);
    }

}
