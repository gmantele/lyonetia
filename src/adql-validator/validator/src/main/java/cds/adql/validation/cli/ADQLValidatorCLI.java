package cds.adql.validation.cli;

import cds.adql.validation.ADQLValidator;
import cds.adql.validation.ParsersConfiguration;
import cds.adql.validation.cli.jcommander.CustomUsageFormatter;
import cds.adql.validation.parser.adql.ADQLParser;
import cds.adql.validation.parser.validationset.ValidationSetParseException;
import cds.adql.validation.report.ADQLValidationReport;
import cds.adql.validation.report.QueryValidationReport;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class running the command line version of the ADQL Validator.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (03/2026)
 */
public class ADQLValidatorCLI {

    private final JCommander commandLineInterface;

    @Parameter(required = true,
               description="(FILE|DIRECTORY)...")
    private List<String> files = new ArrayList<>(5);

    @Parameter(names={"--recursive", "-r"}, description="Browse directories recursively.")
    private boolean recursive = false;

    @Parameter(help=true,
               names={"--help", "-h"},
               description="Display this help.")
    private boolean help = false;

    @Parameter(names={"--quiet", "-q"},
               description="Nothing displayed. Use the exit status code to get the global validation result.")
    private boolean quiet = false;

    @Parameter(names="--show-all",
               description="Display successful and failed tests.")
    private boolean showAll = false;

    @Parameter(names="--no-stats",
               description="Hide the statistics of the whole validation session.")
    private boolean noStats = false;

    @Parameter(names={"--format","-f"},
               description="Report's output format. Supported values: txt (or text), md (or markdown).")
    private OutputFormat format = OutputFormat.TEXT;

    private enum OutputFormat {
        TXT,
        TEXT,
        MD,
        MARKDOWN
    }

    public static void main(final String[] cliParameters)
    {
        final ADQLValidatorCLI validatorRunner = new ADQLValidatorCLI(cliParameters);
        validatorRunner.run();
    }

    public ADQLValidatorCLI(final String[] cliParameters){
        commandLineInterface = createCLI(this, cliParameters);
    }

    private JCommander createCLI(final ADQLValidatorCLI validatorRunner, final String[] cliParameters)
    {
        final JCommander cli = JCommander.newBuilder()
                                         .addObject(validatorRunner)
                                         .build();

        cli.setUsageFormatter(new CustomUsageFormatter(cli));
        cli.setProgramName("java -jar adqlvalidator.jar");

        cli.parse(cliParameters);

        return cli;
    }

    /**
     * Run the ADQL Validator with the parsed arguments.
     *
     * <p><i><b>Note:</b>
     *  All parsed arguments are stored in this instance of
     *  {@link ADQLValidatorCLI}. The given parameter - commandLineInterface -
     *  is the tool used to parse the arguments. It is useful here only to get
     *  the help/usage of this command line program.
     * </i></p>
     */
    private void run() {
        if (help)
            commandLineInterface.usage();
        else
        {
            final List<ADQLValidationReport> allReports = runValidationForAllInputFiles();
            printAllValidationReports(allReports);
        }
    }

    private List<ADQLValidationReport> runValidationForAllInputFiles()
    {
        final List<ADQLValidationReport> allReports = new ArrayList<>();
        try
        {
            final List<ADQLParser> parsers    = ParsersConfiguration.getParsers();
            final ADQLValidator    validator  = new ADQLValidator(parsers);

            for (String filePath : files)
                allReports.addAll(validateInputFile(filePath, validator));
        }
        catch(Exception ex){
            printMessage("FATAL: "+ex.getMessage()+" (error: "+ex.getClass().getName()+")");
        }

        return allReports;
    }

    private List<ADQLValidationReport> validateInputFile(final String filePath, final ADQLValidator validator)
    {
        final File file = new File(filePath);

        if (file.exists())
            return validate(file, validator);
        else
        {
            printMessage("ERROR: file not found! (" + filePath + ")");
            return Collections.emptyList();
        }
    }

    private void printAllValidationReports(final List<ADQLValidationReport> reports)
    {
        // TODO Currently, a basic global validation report! See later for a more detailed one in the asked format.

        long countQueries = 0;
        long countAll = 0;
        long countSucceeded = 0;

        for(ADQLValidationReport report : reports){
            Iterator<Map.Entry<UUID, QueryValidationReport>> itQueries = report.getQueryReports();
            while(itQueries.hasNext()){
                final QueryValidationReport queryReport = itQueries.next().getValue();
                countQueries++;
                countAll += queryReport.size();
                countSucceeded += queryReport.countPassed();
            }
        }

        printMessage("Nb queries = "+countQueries+", Nb Tests = "+countAll+", Nb Succeeded = "+countSucceeded);

        // Add a statistics collector, if asked for:
            /* TODO Review how to get run statistics
            StatCollector statCollector = null;
            if (!noStats) {
                statCollector = new StatCollector();
                validator.addListener(statCollector);
            }
            */

        // Append the result reporter, if not quiet:
            /* TODO Review how to display or output the validation report
            if (!quiet) {
                // ...create the reporter:
                final ValidatorListener reporter = switch(format) {
                                                        case MD, MARKDOWN -> new MarkdownReport();
                                                        default -> new TextReport();
                                                    };
                // ...filter its output:
                reporter.setShowOnlyFailures(!showAll);
                // ...associate the reporter to  the stats collector, if any:
                reporter.setValidationStats(statCollector);
                // ...give this reporter to the validator:
                validator.addListener(reporter);
            }
            */
    }

    private List<ADQLValidationReport> validate(final File file, final ADQLValidator validator)
    {
        if (file.isDirectory())
            return validateDirectory(file, validator);

        else if (file.getName().endsWith(".xml"))
            return List.of(validateRegularFile(file, validator));

        else
            return Collections.emptyList();
    }

    private File[] listDirectoryContent(final File directory){
        final File[] dirContent = directory.listFiles();
        return Objects.requireNonNullElseGet(dirContent, () -> new File[0]);
    }

    private List<ADQLValidationReport> validateDirectory(final File file, final ADQLValidator validator) {
        final List<ADQLValidationReport> reports = new ArrayList<>();

        // Sort files by alphabetic order:
        File[] sortedFiles = listDirectoryContent(file);
        Arrays.sort(sortedFiles, Comparator.comparing(File::getAbsolutePath));

        // Now try to validate all of them:
        for (File f : sortedFiles)
        {
            if (!f.isDirectory() || recursive)
                reports.addAll(validate(f, validator));
        }

        return reports;
    }

    private ADQLValidationReport validateRegularFile(final File file, final ADQLValidator validator)
    {
        try {
            checkXMLDocument(file, validator);
            return validateQuerySet(file, validator);
        }
        catch (IOException e) {
            printMessage("ERROR: Cannot read the input file ("+file.getAbsolutePath()+")! Cause: "+e.getMessage());
        }
        catch (ValidationSetParseException e) {
            printMessage("ERROR: Failed to parse the XML file ("+file.getAbsolutePath()+")! Cause: "+e.getMessage());
        }
        return new ADQLValidationReport();
    }

    private void checkXMLDocument(final File file, final ADQLValidator validator) throws IOException, ValidationSetParseException {
        try(InputStream stream = new FileInputStream(file)) {
            validator.checkXML(stream);
        }
    }

    private ADQLValidationReport validateQuerySet(final File file, final ADQLValidator validator) throws IOException, ValidationSetParseException {
        try(InputStream stream = new FileInputStream(file)) {
            // ...and validate the tests set:
            return validator.validate(stream, "File (" + file.getAbsolutePath() + ")");
        }
    }

    private void printMessage(final String message){
        commandLineInterface.getConsole().println(message);
    }

}
