package diff;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.xml.XMLDocumentUtils;
import edu.berkeley.cs.jqf.examples.xml.XmlDocumentGenerator;
import edu.berkeley.cs.jqf.examples.common.Dictionary;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

@RunWith(Mu2.class)
public class MavenTest {


    public Model testWithInputStream(InputStream in) {
        ModelReader reader = new DefaultModelReader();
        Model model = null;
        try {
            model = reader.read(in, null);
            Assert.assertNotNull(model);
        } catch (IOException e) {
            Assume.assumeNoException(e);
        }
        return model;
    }


    @Fuzz
    public void fuzzWithGenerator(@From(XmlDocumentGenerator.class)
                                      @Dictionary("dictionaries/maven-model.dict") Document dom) {
        testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @Fuzz
    public void debugWithGenerator(@From(XmlDocumentGenerator.class)
                                      @Dictionary("dictionaries/maven-model.dict") Document dom) {
        System.out.println(XMLDocumentUtils.documentToString(dom));
        testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @Diff
    public Model testWithGenerator(@From(XmlDocumentGenerator.class)
                                      @Dictionary("dictionaries/maven-model.dict") Document dom) {
        return testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

}
