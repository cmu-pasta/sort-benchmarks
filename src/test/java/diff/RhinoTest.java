package diff;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.examples.js.JavaScriptCodeGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

@RunWith(Mu2.class)
public class RhinoTest {

    private Context context;

    @Before
    public void initContext() {
        context = Context.enter();
    }

    @After
    public void exitContext() {
        context.exit();
    }

    public Script testWithString(@From(AsciiStringGenerator.class) String input) {
        Script script = null;
        try {
            script = context.compileString(input, "input", 0, null);
        } catch (EvaluatorException e) {
            Assume.assumeNoException(e);
        }
        return script;

    }

    @Fuzz
    public void debugWithString(@From(AsciiStringGenerator.class) String code) {
        System.out.println("\nInput:  " + code);
        testWithString(code);
        System.out.println("Success!");
    }

    @Test
    public void smallTest() {
        testWithString("x = 3 + 4");
        testWithString("x <<= undefined");
    }

    @Fuzz
    public void testWithInputStream(InputStream in) throws IOException {
        try {
            Script script = context.compileReader(new InputStreamReader(in), "input", 0, null);
        } catch (EvaluatorException e) {
            Assume.assumeNoException(e);
        }
    }

    @Fuzz
    public void debugWithInputStream(InputStream in) throws IOException {
        String input = IOUtils.toString(in, StandardCharsets.UTF_8);
        debugWithString(input);
    }

    @Fuzz
    public void fuzzWithGenerator(@From(JavaScriptCodeGenerator.class) String code) {
        testWithString(code).toString();
    }

    @Diff
    public String testWithGenerator(@From(JavaScriptCodeGenerator.class) String code) {
        return testWithString(code).toString();
    }

    @Fuzz
    public void debugWithGenerator(@From(JavaScriptCodeGenerator.class) String code) {
        debugWithString(code);
    }



}
