package diff;

import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.examples.xml.XmlDocumentGenerator;
import org.junit.Assume;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

@RunWith(Mu2.class)
public class JacksonXMLTest{

    private ObjectMapper xmlMapper = new XmlMapper();

    @Diff
    public Object testReadValue(@From(AsciiStringGenerator.class) String input) {
        Object output = null;
        try {
            output = xmlMapper.readValue(input, Object.class);
        } catch (JsonProcessingException e) {
        }
        return output;
    }
}
