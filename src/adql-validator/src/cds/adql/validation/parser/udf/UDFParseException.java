package cds.adql.validation.parser.udf;

public class UDFParseException extends Exception{
    public UDFParseException() {
        super();
    }

    public UDFParseException(String message) {
        super(message);
    }

    public UDFParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UDFParseException(Throwable cause) {
        super(cause);
    }
}
