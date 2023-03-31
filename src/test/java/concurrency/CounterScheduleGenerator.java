package concurrency;

import cmu.pasta.cdiff.schedule.ListSchedule;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import java.util.List;

public class CounterScheduleGenerator extends Generator<ListSchedule> {
    public CounterScheduleGenerator() {
        super(ListSchedule.class);
    }

    public static int num = 0;

    @Override
    public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        int seed = num++;//sourceOfRandomness.seed();
        //System.out.println("sourceOfRandomness " + sourceOfRandomness + " with seed " + seed);
        switch (seed) {
            case 0: return new ListSchedule(List.of(0, 1, 0, 0, 1, 1, 0, 0, 0)); //start,t1,t1,t2,t2,main
            case 1: return new ListSchedule(List.of(0, 1, 1, 1, 0, 0, 0, 0, 0)); //start,t2,t2,t1,t1,main
            case 2: return new ListSchedule(List.of(1, 0, 0, 1, 0, 1, 0, 0, 0)); //start,t1,t2,t1,t2,main
            case 3: return new ListSchedule(List.of(2, 1, 1, 0, 1, 0, 0, 0, 0)); //start,t2,t1,t2,t1,main
            default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0));
        }
    }
}
