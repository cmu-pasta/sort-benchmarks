package diff;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import edu.berkeley.cs.jqf.examples.common.AsciiStringGenerator;

public class OptionsGenerator extends Generator<Options> {

    private static int maxOptions;
    private AsciiStringGenerator stringGenerator;

    public OptionsGenerator() {
        super(Options.class);
        this.maxOptions = 5;
        this.stringGenerator = new AsciiStringGenerator();
    }

    @Override
    public Options generate(SourceOfRandomness random, GenerationStatus status) {
        int numOptions = random.nextInt(0, maxOptions);
        Options options = new Options();
        for (int i = 0; i < numOptions; i++) {
            Option option = generateOption(random, status);
            options.addOption(generateOption(random, status));
        }
        return options;
    }

    public Option generateOption(SourceOfRandomness random, GenerationStatus status) {
        Option option = new Option(
                stringGenerator.generate(random, status),
                stringGenerator.generate(random, status),
                random.nextBoolean(),
                stringGenerator.generate(random, status)
        );
        return option;
    }

}