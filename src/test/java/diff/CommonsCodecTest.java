package diff;

import cmu.pasta.mu2.diff.Comparison;
import cmu.pasta.mu2.diff.Diff;
import cmu.pasta.mu2.diff.Mu2;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;
import org.junit.runner.RunWith;
import org.apache.commons.codec.binary.*;

import java.nio.charset.StandardCharsets;

@RunWith(Mu2.class)
public class CommonsCodecTest {

    private Base64 base64 = new Base64();

    @Diff
    public String testEncodeDecode(@From(AsciiStringGenerator.class) String input) {
        byte[] encoded = base64.encode(input.getBytes());
        String decoded = new String(base64.decode(encoded));
        return decoded;
    }
}
