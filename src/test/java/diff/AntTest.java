package diff;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.xml.XMLDocumentUtils;
import edu.berkeley.cs.jqf.examples.xml.XmlDocumentGenerator;
import edu.berkeley.cs.jqf.examples.common.Dictionary;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.helper.ProjectHelperImpl;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

@RunWith(Mu2.class)
public class AntTest {

    private File serializeInputStream(InputStream in) throws IOException {
        Path path = Files.createTempFile("build", ".xml");
        try (BufferedWriter out = Files.newBufferedWriter(path)) {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        }
        return path.toFile();
    }

    public NewProject testWithInputStream(InputStream in) {
        File buildXml = null;
        NewProject project = new NewProject();
        try {
            buildXml = serializeInputStream(in);
            ProjectHelperImpl p = new ProjectHelperImpl();
            p.parse(project, buildXml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BuildException e) {
            Assume.assumeNoException(e);
        } finally {
            if (buildXml != null) {
                buildXml.delete();
            }
        }
        return project;
    }

    @Fuzz
    public void fuzzWithGenerator(@From(XmlDocumentGenerator.class)
                                      @Dictionary("dictionaries/ant-project.dict") Document dom) {
        testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @Fuzz
    public void debugWithGenerator(@From(XmlDocumentGenerator.class)
                                      @Dictionary("dictionaries/ant-project.dict") Document dom) {
        System.out.println(XMLDocumentUtils.documentToString(dom));
        testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @Diff(cmp = "compare")
    public NewProject testWithGenerator(@From(XmlDocumentGenerator.class)
                                      @Dictionary("dictionaries/ant-project.dict") Document dom) {
        NewProject p = testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
        return p;
    }

    @Comparison
    public static Boolean compare(NewProject p1, NewProject p2) {
      return
        p1.getProperties().equals(p2.getProperties()) &&
        p1.getDefaultTarget().equals(p2.getDefaultTarget()) &&
        p1.getName().equals(p2.getName()) &&
        p1.getDescription().equals(p2.getDescription());
    }
}
