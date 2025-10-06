package cds.adql.validation.parser.adql;

import cds.adql.validation.ThreadPool;
import cds.adql.validation.parser.adql.exceptions.RunParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version (04/2025)
 */
class RunCommandTest {

    static ThreadPool threadPool = null;

    @BeforeAll
    static void beforeAll() {
        threadPool = new ThreadPool(10);
    }

    @AfterAll
    static void afterAll() {
        threadPool.stop();
    }


    /* **********************************************************************
     *  SET_TIMEOUT */

    @Test
    void setTimeout_ShouldChange_WhenIncrementingTimeout() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("echo");
        final int formerTimeout = cmd.getTimeout();
        final int expectedTimeout = formerTimeout+2;

        // When:
        cmd.setTimeout(expectedTimeout);

        // Then:
        assertEquals(expectedTimeout, cmd.getTimeout());
    }

    @Test
    void setTimeout_ShouldNotChange_WhenNegativeTime() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("echo");
        final int formerTimeout = cmd.getTimeout();

        for(int timeout : new int[]{0, -1})
        {
            // When:
            cmd.setTimeout(timeout);

            // Then:
            assertEquals(formerTimeout, cmd.getTimeout());
        }
    }


    /* **********************************************************************
     *  SET_DIRECTORY */

    @Test
    void setDirectory_ShouldChange_WhenSettingANewDirectory() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("echo");
        final File formerDirectory = cmd.getDirectory();
        final File expectedDirectory = new File("test-resources/parsers/");
        assertNotEquals(expectedDirectory, formerDirectory);

        // When:
        cmd.setDirectory(expectedDirectory);

        // Then:
        assertEquals(expectedDirectory, cmd.getDirectory());
    }

    @Test
    void setDirectory_ShouldNotChange_WhenNull() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("echo");
        final File formerDirectory = new File("test-resources/parsers/");
        final File defaultDirectory = new File(".").getAbsoluteFile();
        assertNotEquals(formerDirectory, defaultDirectory);
        cmd.setDirectory(formerDirectory);
        assertEquals(formerDirectory, cmd.getDirectory());

        // When:
        cmd.setDirectory(null);

        // Then:
        assertEquals(defaultDirectory, cmd.getDirectory());
    }


    /* **********************************************************************
     *  CALL */

    @Test
    void run_ShouldReturnSameMessage_WhenSimpleEcho() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("echo", "hello", "world");

        // When:
        final CommandOutput output = cmd.run();

        // Then:
        assertEquals("hello world", output.output);
    }

    @Test
    void run_ShouldFail_WhenUnknownCommand() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("fooBla");

        // When:
        try{
            cmd.run();
            fail("This test should have failed because the command to startValidation does not exist!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(RunParserException.class, ex.getClass());
            assertEquals(IOException.class, ex.getCause().getClass());
            assertEquals("Cannot startValidation program \"fooBla\": error=2, No such file or directory", ex.getCause().getMessage());
        }
    }

    @Test
    void run_ShouldFail_WhenTooLong() throws RunParserException {
        // Given:
        final int TOO_LONG_TIME = 5;
        final RunCommand cmd = new RunCommand("sleep", Integer.toString(TOO_LONG_TIME));
        cmd.setTimeout(TOO_LONG_TIME-2);

        // When:
        try {
            cmd.run();
            fail("This test should have failed because the command startValidation for a too long time!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(RunParserException.class, ex.getClass());
            assertEquals("Time out! Cause: the command execution was too long (> 3 seconds)", ex.getMessage());
        }
    }

    @Test
    void run_ShouldFail_WhenCommandReturnedError() throws RunParserException {
        // Given:
        final RunCommand cmd = new RunCommand("ls", "--foo");

        // When:
        try {
            cmd.run();
            fail("This test should have failed because the command was canceled!");
        }
        // Then:
        catch(Exception ex){
            assertEquals(RunParserException.class, ex.getClass());
            assertEquals("Failed command (bash -c ls) execution!\nCause: ls: unrecognized option '--foo'\nTry 'ls --help' for more information.", ex.getMessage());
        }
    }
}