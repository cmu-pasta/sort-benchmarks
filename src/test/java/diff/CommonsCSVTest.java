package diff;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

import java.io.IOException;
import java.util.List;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(Mu2.class)
public class CommonsCSVTest {

    @Diff
    public String testCSVParser(@From(AsciiStringGenerator.class) String input) {
        List<CSVRecord> records = null;
        try {
            CSVParser parser = CSVParser.parse(input, CSVFormat.EXCEL);
            records = parser.getRecords();
        } catch (IOException e) {
            Assume.assumeNoException(e);
        }
        return records.toString();
    }

    @Fuzz
    public void fuzzCSVParser(@From(AsciiStringGenerator.class) String input) throws IOException {
        List<CSVRecord> records = null;
        try {
            CSVParser parser = CSVParser.parse(input, CSVFormat.EXCEL);
            records = parser.getRecords();
        } catch (IOException e) {
            Assume.assumeNoException(e);
        }
    }

}
