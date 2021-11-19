package diff;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import org.apache.commons.cli.*;
import org.junit.Assume;
import org.junit.runner.RunWith;


@RunWith(Mu2.class)
public class CommonsCLITest {

    @Diff
    public CommandLine testCommonsCLIParser(@From(OptionsGenerator.class) Options options, @From(AsciiStringGenerator.class) String input) {
        String[] args = new String[] { input };
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            return cmd;
        } catch(ParseException e) {
            Assume.assumeNoException(e);
        }
        return cmd;
    }

}
