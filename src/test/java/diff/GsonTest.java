package diff;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.ArbitraryLengthStringGenerator;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import org.junit.Assume;
import org.junit.runner.RunWith;

@RunWith(Mu2.class)
public class GsonTest {

    @Diff
    public Object testJSONParser(@From(AsciiStringGenerator.class) String input) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setLenient().create();
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

}