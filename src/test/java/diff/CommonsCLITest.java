package diff;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Objects;


@RunWith(Mu2.class)
public class CommonsCLITest {

    @Test
    public void test() {
        //[ Options: [ short {} ] [ long {} ], -={"
        //causes StringOutOfBoundsException: begin 0, end 1, length 0
        //  at java.base/java.lang.String.checkBoundsBeginEnd(String.java:3734)
        //	at java.base/java.lang.String.substring(String.java:1903)
        //	at org.apache.commons.cli.DefaultParser.isJavaProperty(DefaultParser.java:583)
        Options options = new Options();
        String[] args = {"-={\""};
        try {
            parser.parse(options, args);
        } catch(ParseException e) {
            Assume.assumeNoException(e);
        }
    }

    private CommandLineParser parser = new DefaultParser();

    @Diff(cmp = "equalsCommandLine")
    public CommandLine testParser(@From(OptionsGenerator.class) Options options, @From(AsciiStringGenerator.class) String input) {
        String[] args = new String[] { input };
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch(ParseException e) {
            Assume.assumeNoException(e);
        }
        return cmd;
    }

    @Comparison
    public static Boolean equalsCommandLine(CommandLine cl1, CommandLine cl2) {
        if(cl1 == cl2) return true;
        if(cl1 == null || cl2 == null) return false;
        return Objects.equals(cl1.getArgList(), cl2.getArgList()) && Arrays.equals(cl1.getOptions(), cl2.getOptions());
    }

    @Fuzz
    public void fuzzParser(@From(OptionsGenerator.class) Options options, @From(AsciiStringGenerator.class) String input) {
        String[] args = new String[] { input };
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch(ParseException e) {
            Assume.assumeNoException(e);
        }
    }
}
