package concurrency;

import cmu.pasta.cdiff.RandomSchedule;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

public class RandomScheduleGenerator extends Generator<RandomSchedule> {

    public RandomScheduleGenerator() {
        super(RandomSchedule.class);
    }

    @Override
    public RandomSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        return new RandomSchedule(sourceOfRandomness.nextInt());
    }
}
