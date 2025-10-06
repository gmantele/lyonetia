package cds.adql.validation;

import cds.adql.validation.parser.adql.ADQLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Helper class to get the configuration of ADQL parsers to use for validation.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (05/2025)
 */
public class ParsersConfiguration {

    public static final String DEFAULT_PARSERS_CONFIGURATION_FILE = "parsers.properties";

    static final String KEY_PARSERS        = "parsers";
    static final String KEY_SUFFIX_COMMAND = "command";

    private ParsersConfiguration(){}

    public static List<ADQLParser> getParsers() throws IncorrectValidatorConfigurationException {
        return getParsers(DEFAULT_PARSERS_CONFIGURATION_FILE);
    }

    public static List<ADQLParser> getParsers(final String configFilePath) throws IncorrectValidatorConfigurationException {
        final Properties parsersProps = getParsersProperties(configFilePath);
        return getConfiguredParsers(parsersProps);
    }

    private static Properties getParsersProperties(final String configFilePath) throws IncorrectValidatorConfigurationException {
        final File configFile = getParsersConfigurationFile(configFilePath);

        try(final InputStream parsersListStream = new FileInputStream(configFile))
        {
            final Properties parsersProps = new Properties();
            parsersProps.load(parsersListStream);
            return parsersProps;
        }
        catch (IOException ioe) {
            throw new IncorrectValidatorConfigurationException("Failed to read the configuration file! ("+configFile.getAbsolutePath()+")", ioe);
        }
    }

    private static File getParsersConfigurationFile(String configFilePath) throws IncorrectValidatorConfigurationException {
        if (configFilePath == null || configFilePath.isBlank())
            configFilePath = DEFAULT_PARSERS_CONFIGURATION_FILE;

        final URL rootURL = ParsersConfiguration.class.getClassLoader().getResource(configFilePath);

        if (rootURL == null)
            throw new IncorrectValidatorConfigurationException("No parsers configuration file found! (expected: "+ configFilePath +")");

        return new File(rootURL.getPath());
    }

    private static List<ADQLParser> getConfiguredParsers(final Properties parsersProps) throws IncorrectValidatorConfigurationException {
        final ArrayList<ADQLParser> lstParsers = new ArrayList<>(parsersProps.size());

        final String[] parserNames = getParserNames(parsersProps);

        for(String parserName : parserNames){
            final String parserCmd = getParserCommand(parsersProps, parserName.trim());
            final ADQLParser parser = new ADQLParser(parserName, parserCmd);
            lstParsers.add(parser);
        }

        return lstParsers;
    }

    private static String[] getParserNames(final Properties parsersProps) throws IncorrectValidatorConfigurationException {
        final String parsers = parsersProps.getProperty(KEY_PARSERS);

        if (parsers == null)
            throw new IncorrectValidatorConfigurationException("Missing Properties' key '"+KEY_PARSERS+"'! This key MUST be provided. It MUST be set with a comma separated list of the ADQL parsers to use.");

        return parsers.split(",");
    }

    private static String getParserCommand(final Properties parsersProps, final String parserName) throws IncorrectValidatorConfigurationException {
        final String keyCommand = parserName + "." + KEY_SUFFIX_COMMAND;
        final String command    = parsersProps.getProperty(keyCommand);

        if (command == null)
            throw new IncorrectValidatorConfigurationException("Missing configuration key '"+keyCommand+"'! This MUST be provided in order to startValidation the parser '"+parserName+"'.");
        else
            return command;
    }
}
