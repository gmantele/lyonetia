package cds.adql.validation.parser.validationset;

/**
 * Exception thrown while parsing the XML representation of a
 * {@link cds.adql.validation.query.ValidationSet}.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 * @see ValidationSetParser
 */
public class ValidationSetParseException extends Exception {

    public ValidationSetParseException() {}

    public ValidationSetParseException(String s) {
        super(s);
    }

    public ValidationSetParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ValidationSetParseException(Throwable throwable) {
        super(throwable);
    }

}
