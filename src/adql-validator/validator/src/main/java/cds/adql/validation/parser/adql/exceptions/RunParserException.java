package cds.adql.validation.parser.adql.exceptions;

public class RunParserException extends ADQLParseException {
    public RunParserException() { }

    public RunParserException(String s) {
        super(s);
    }

    public RunParserException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RunParserException(Throwable throwable) {
        super(throwable);
    }
}
