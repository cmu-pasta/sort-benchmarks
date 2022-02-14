package diff;

import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.Compiler;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.js.JavaScriptCodeGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;

@RunWith(Mu2.class)
public class BellaClosureTest {
    static {
        // Disable all logging by Closure passes
        LogManager.getLogManager().reset();
    }

    private Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
    private CompilerOptions options = new CompilerOptions();
    private SourceFile externs = SourceFile.fromCode("externs", "");

    @Before
    public void initCompiler() {
        // Don't use threads
        compiler.disableThreads();
        // Don't print things
        options.setPrintConfig(false);
        // Enable all safe optimizations
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
    }

    @Fuzz
    public void testWithGenerator(@From(JavaScriptCodeGenerator.class) String code) {
        SourceFile input = SourceFile.fromCode("input", code);
        Result result = compiler.compile(externs, input, options);
        Assume.assumeTrue(result.success);
    }
}
