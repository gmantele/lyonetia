package cds.adql.validation.parser.adql;

import cds.adql.validation.parser.adql.exceptions.RunParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to startValidation a command line process.
 *
 * <p>
 *     A maximum execution time is set by default: {@value #DEFAULT_TIME_FOR_COMPLETION} seconds.
 *     It can be changed using {@link #setTimeout(int)}.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 2.0 (05/2025)
 */
public class RunCommand {

    static final Logger LOGGER = Logger.getLogger(RunCommand.class.getName());

    private static final int TIME_FOR_DESTRUCTION = 1; // seconds

    private final String command;
    private final ProcessBuilder processBuilder;

    private File stdOutputFile = null;

    private static final int DEFAULT_TIME_FOR_COMPLETION  = 3; // seconds
    private int timeForCompletion = DEFAULT_TIME_FOR_COMPLETION;

    private Process process = null;

    public RunCommand(final String commandName, final String... commandParameters) {
        final String[] commandArray = makeCommandArray(commandName, commandParameters);

        this.processBuilder = makeBuilder(commandArray);
        this.command        = String.join(" ", commandArray);
    }

    private static String[] makeCommandArray(final String name, final String[] parameters){
        final String[] commandArray = new String[parameters.length+1];

        commandArray[0] = name;

        int i = 1;
        for(String p : parameters)
            commandArray[i++] = p;

        return commandArray;
    }

    private ProcessBuilder makeBuilder(final String[] commandArray){
        final ProcessBuilder builder = new ProcessBuilder();
        builder.command(commandArray);
        builder.redirectErrorStream(true);
        return builder;
    }

    /**
     * Get the time to wait for the command to end.
     *
     * <p><b>Note:</b>
     *   The total execution time may be more than the given time for completion.
     *   In case the command execution time reaches the given time, the command
     *   will be gracefully stopped. If after some time
     *   ({@value #TIME_FOR_DESTRUCTION} sec.), the command is still running,
     *   the command will be forcibly killed. In the case the command execution
     *   succeeds within the given time for completion, a bit more time may be
     *   taken to wait for the result to be fetched.
     * </p>
     *
     * @return  Time before stopping the command.
     */
    public int getTimeout(){
        return timeForCompletion;
    }

    /**
     * Set the time to wait for the command to end.
     *
     * <p>
     *     This function has no effect if the given time is negative or zero.
     * </p>
     *
     * <p><b>Note:</b>
     *  The total execution time may be more than the given time for completion.
     *  In case the command execution time reaches the given time, the command
     *  will be gracefully stopped. If after some time
     *  ({@value #TIME_FOR_DESTRUCTION} sec.), the command is still running,
     *  the command will be forcibly killed. In the case the command execution
     *  succeeds within the given time for completion, a bit more time may be
     *  taken to wait for the result to be fetched.
     * </p>
     *
     * @param timeForCompletion Time in seconds before the command is killed.
     */
    public void setTimeout(final int timeForCompletion){
        if (timeForCompletion > 0)
            this.timeForCompletion = timeForCompletion;
    }

    /**
     * Get the directory in which the command will startValidation.
     *
     * @return Directory in which the command must startValidation.
     */
    public File getDirectory(){
        return processBuilder.directory();
    }

    /**
     * Change the directory in which the command must startValidation.
     *
     * <p>
     *     When the given directory is NULL, the working directory of the
     *     current Java process will be set.
     *     See {@link ProcessBuilder#directory(File)} for more details.
     * </p>
     *
     * @param directory Directory in which the command must startValidation.
     */
    public void setDirectory(File directory){
        if (directory == null)
            directory = new File(".").getAbsoluteFile();
        processBuilder.directory(directory);
    }

    public CommandOutput run() throws RunParserException {
        try {
            final long startTime = System.currentTimeMillis();
            initOutputFile();
            runCommand();
            return getResult(startTime);
        }
        finally {
            deleteOutputFile();
        }
    }

    private void runCommand() throws RunParserException {
        try {
            process = processBuilder.start();

            logPID();
        }
        catch(IOException ioe){
            throw new RunParserException("Failed to create the process!", ioe);
        }
    }

    private void initOutputFile() throws RunParserException {
        try{
            stdOutputFile = File.createTempFile("adql-validator-cmd-output", ".log");
            processBuilder.redirectOutput(stdOutputFile);
        }
        catch(Exception ex){
            throw new RunParserException("Failed to create a temporary output file for a command execution! Cause: "+ex.getMessage(), ex);
        }

    }

    private void logPID(){
        final long pid = process.toHandle().pid();
        LOGGER.info(() -> "Running command (" + command + ") with PID: " + pid);
    }

    private CommandOutput getResult(final long startTime) throws RunParserException {
        final boolean isFinished = waitForCompletion();
        if (isFinished) {
            final String output = getResultIfSuccessful();
            final long duration = System.currentTimeMillis() - startTime;
            return new CommandOutput(output, duration);
        }
        else {
            LOGGER.severe("Process too long!");
            stop();
            throw new RunParserException("Time out! Cause: the command execution was too long (> " + timeForCompletion + " seconds)");
        }
    }

    private boolean waitForCompletion() throws RunParserException {
        try {
            return process.waitFor(timeForCompletion, TimeUnit.SECONDS);
        }
        catch(InterruptedException ie) {
            LOGGER.severe("Interrupted while waiting for command to complete! (isAlive : "+process.isAlive()+")");
            stop();
            Thread.currentThread().interrupt();
            throw new RunParserException(ie);
        }
    }

    private String getResultIfSuccessful() throws RunParserException {
        final int exitCode = process.exitValue();
        final String outContent = getOutputContent();

        if (exitCode == 0)
            return outContent;
        else
            throw new RunParserException("Failed command (bash -c ls) execution!\nCause: " + outContent);
    }

    private String getOutputContent() throws RunParserException {
        try(final BufferedReader reader = new BufferedReader(new FileReader(stdOutputFile)))
        {
            final StringJoiner strJoiner = new StringJoiner("\n");
            String line;

            /* Implementation comment:
             *   BufferedReader.readLine() is a blocking function that does not
             *   react to Thread interruptions. But considering the expected and
             *   assumed shortness of the file, it should be a really quick
             *   content extraction.
             */

            while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null)
                strJoiner.add(line);

            if (Thread.currentThread().isInterrupted())
                strJoiner.add("[[ ERROR: Command output extraction interrupted! ]]");

            return strJoiner.toString();
        }
        catch (IOException ioe) {
            throw new RunParserException("Unexpected command output extraction failure! Cause: "+ioe.getMessage(), ioe);
        }
    }

    private void deleteOutputFile(){
        try {
            Files.delete(stdOutputFile.toPath());
        }
        catch(IOException ioe){
            LOGGER.warning("Unable to delete the temporary file for command output: '"+stdOutputFile.getAbsolutePath()+"'! Cause: "+ioe.getMessage());
            stdOutputFile.deleteOnExit();
        }
    }

    /**
     * Stop this command.
     *
     * <p>
     *     This function tries to gracefully stop the command execution. After
     *     some time (by default {@value #TIME_FOR_DESTRUCTION} ; configurable
     *     with {@link #setTimeout(int)}), it tries to stop it forcefully.
     * </p>
     */
    public void stop() {
        LOGGER.info("Destroying the process...");

        try
        {
            process.destroy();
            process.waitFor(TIME_FOR_DESTRUCTION, TimeUnit.SECONDS);

            if (process.isAlive()) {
                LOGGER.severe("Failed to stop nicely the process! Forcing its destruction...");
                process.destroyForcibly().waitFor(TIME_FOR_DESTRUCTION, TimeUnit.SECONDS);
            }else
                LOGGER.info("Process destroyed!");
        }
        catch(InterruptedException ie){
            Thread.currentThread().interrupt();
            LOGGER.severe("Process destruction interrupted! Cause: "+ie.getMessage());
        }
        catch(Exception e) {
            LOGGER.severe("Process destruction error! Cause: "+e.getMessage());
        }
    }

}
