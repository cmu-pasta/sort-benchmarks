package diff;

import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(Mu2.class)
public class GsonTest {

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.setLenient().create();

    @Diff
    public Object testJSONParser(@From(AsciiStringGenerator.class) String input) {
        Object out = null;
        try {
            out = gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
        return out;
    }

    @Fuzz
    public void fuzzJSONParser(@From(AsciiStringGenerator.class) String input) {
        Object out = null;
        try {
            out = gson.fromJson(input, Object.class);
        } catch (JsonSyntaxException e) {
            Assume.assumeNoException(e);
        } catch (JsonIOException e) {
            Assume.assumeNoException(e);
        }
    }

}
