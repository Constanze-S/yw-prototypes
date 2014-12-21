package org.yesworkflow;

/* This file is an adaptation of TestKuratorAkka.java in the org.kurator.akka
 * package as of 18Dec2014.
 */

import org.yesworkflow.util.YesWorkflowTestCase;

public class TestYesWorkflowCLI extends YesWorkflowTestCase {

    private static String EXPECTED_HELP_OUTPUT =
        ""                                                              + EOL +
        "Option                     Description                      "  + EOL +
        "------                     -----------                      "  + EOL +
        "-c, --command <command>    command to YesWorkflow           "  + EOL +
        "-d, --database <database>  path to database file for storing"  + EOL +
        "                             extracted workflow graph       "  + EOL +
        "-h, --help                 display help                     "  + EOL +
        "-s, --source <script>      path to source file to analyze   "  + EOL;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testYesWorkflowCLI_NoArgs() throws Exception {
        String[] args = {};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Usage error: No command provided to YesWorkflow"   + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testYesWorkflowCLI_HelpOption() throws Exception {
        String[] args = {"--help"};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testYesWorkflowCLI_HelpOption_Abbreviation() throws Exception {
        String[] args = {"-h"};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testYesWorkflowCLI_CommandOption_NoArgument() throws Exception {
        String[] args = {"-c"};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Usage error: Option c/command requires an argument"     + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testYesWorkflowCLI_SourceOption_NoArgument() throws Exception {
        String[] args = {"-s"};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Usage error: Option s/source requires an argument"      + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testYesWorkflowCLI_Extract_NoSource() throws Exception {
        String[] args = {"-c", "extract"};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Usage error: No source path provided to extractor"      + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testYesWorkflowCLI_Extract() throws Exception {
        String[] args = {"-c", "extract", "-s", "src/main/resources/example.py"};
        YesWorkflowCLI.runForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }    
}