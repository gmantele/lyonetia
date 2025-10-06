package cds.adql.validation.parser.adql;

/**
 * Object representation of a command execution output.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version (05/2025)
 */
public class CommandOutput {
    /** Textual command execution output. */
    public final String output;

    /** Command execution duration (in milliseconds). */
    public final long duration;

    /**
     * Create an object representation of a command execution output.
     *
     * @param commandOutput Textual output of this command.
     * @param duration      Command execution duration in milliseconds.
     */
    public CommandOutput(final String commandOutput, final long duration) {
        this.output = commandOutput;
        this.duration = duration;
    }
}
