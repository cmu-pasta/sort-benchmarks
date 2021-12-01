package diff;

import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

import java.io.IOException;
import java.util.List;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.runner.RunWith;

@RunWith(Mu2.class)
public class CommonsCSVTest {

    @Diff
    public List<CSVRecord> testCSVParser(@From(AsciiStringGenerator.class) String input) throws IOException {
        CSVParser parser = CSVParser.parse(input, CSVFormat.EXCEL);
        return parser.getRecords();
    }

}
