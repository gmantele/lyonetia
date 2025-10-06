package cds.adql.validation;

public class IncorrectValidatorConfigurationException extends Exception {
    public IncorrectValidatorConfigurationException() {
    }

    public IncorrectValidatorConfigurationException(String message) {
        super(message);
    }

    public IncorrectValidatorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectValidatorConfigurationException(Throwable cause) {
        super(cause);
    }
}
