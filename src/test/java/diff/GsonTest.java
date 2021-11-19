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
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.ArbitraryLengthStringGenerator;
import org.junit.runner.RunWith;

@RunWith(Mu2.class)
public class GsonTest {

    @Diff
    public Object testJSONParser(@From(ArbitraryLengthStringGenerator.class) String input) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(input, Object.class);
    }

}