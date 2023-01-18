package diff;

import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Mu2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

@RunWith(Mu2.class)
public class JacksonDatabindTest{

    private ObjectMapper objectMapper = new ObjectMapper();

    @Diff
    public Object testJsonReadValue(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
           Assume.assumeNoException(e);
        }
        return output;
    }

    @Fuzz(repro="${repro}")
    public void fuzzJsonReadValue(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
           Assume.assumeNoException(e);
        }
    }

    @Diff(cmp = "noncompare")
    public Object testJsonReadValueNoncompare(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = objectMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
           Assume.assumeNoException(e);
        }
        return output;
    }

    @Comparison
    public static Boolean noncompare(Object o1, Object o2) {
        return true;
    }
}
