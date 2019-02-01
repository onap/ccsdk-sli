package org.onap.ccsdk.sli.core.dblib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EncShellCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testDoExecute() throws Exception {
        String expected = "Original value: test" + System.getProperty("line.separator") +
                "Encrypted value: test";
        EncShellCommand encShellCommand = new EncShellCommand();
        encShellCommand.arg = "test";
        encShellCommand.doExecute();
        assertEquals(expected.trim(), outContent.toString().trim());
    }
}