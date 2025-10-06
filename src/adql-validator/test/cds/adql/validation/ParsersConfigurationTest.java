package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParsersConfigurationTest {

    @Test
    void getParsers_ShouldSucceed_WhenDefaultConfigFile() throws IncorrectValidatorConfigurationException {
        final List<ADQLParser> parsers = ParsersConfiguration.getParsers();
        assertNotNull(parsers);
        assertFalse(parsers.isEmpty());
    }

    @Test
    void getParsers_ShouldSucceed_WhenNoConfigFile() throws IncorrectValidatorConfigurationException {
        for(String configFilePath : new String[]{ null, "", "  " }) {
            final List<ADQLParser> parsers = ParsersConfiguration.getParsers(configFilePath);
            assertNotNull(parsers);
            assertFalse(parsers.isEmpty());
        }
    }

    @Test
    void getParsers_ShouldFail_WhenConfigFileDoesNotExist() throws IncorrectValidatorConfigurationException {
        final String nonExistingFilePath = "foo";

        try {
            ParsersConfiguration.getParsers(nonExistingFilePath);
            fail("Should fail when the specified file does not exist!");
        }
        catch(Exception ex){
            assertEquals(IncorrectValidatorConfigurationException.class, ex.getClass());
            assertEquals("No parsers configuration file found! (expected: foo)", ex.getMessage());
        }
    }

    @Test
    void getParsers_ShouldFail_WhenConfigFileDoesNotContainTheParsersKey() throws IncorrectValidatorConfigurationException {
        final String incorrectFilePath = "config-no-parsers-key.properties";

        try {
            ParsersConfiguration.getParsers(incorrectFilePath);
            fail("Should fail when the specified file does not contain the expected key ('"+ParsersConfiguration.KEY_PARSERS+"')!");
        }
        catch(Exception ex){
            assertEquals(IncorrectValidatorConfigurationException.class, ex.getClass());
            assertEquals("Missing Properties' key '"+ParsersConfiguration.KEY_PARSERS+"'! This key MUST be provided. It MUST be set with a comma separated list of the ADQL parsers to use.", ex.getMessage());
        }
    }

    @Test
    void getParsers_ShouldFail_WhenConfigFileWithMissingCommand() throws IncorrectValidatorConfigurationException {
        final String incorrectFilePath = "config-no-command.properties";

        try {
            ParsersConfiguration.getParsers(incorrectFilePath);
            fail("Should fail when the specified file does not contain the expected key ('"+ParsersConfiguration.KEY_PARSERS+"')!");
        }
        catch(Exception ex){
            assertEquals(IncorrectValidatorConfigurationException.class, ex.getClass());
            assertEquals("Missing configuration key 'noCommand."+ParsersConfiguration.KEY_SUFFIX_COMMAND+"'! This MUST be provided in order to startValidation the parser 'noCommand'.", ex.getMessage());
        }
    }
}