package concurrency;

import cmu.pasta.cdiff.schedule.ListSchedule;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.List;

public class VolatileScheduleGenerator extends Generator<ListSchedule> {
    public static int num = 0;

    public VolatileScheduleGenerator() {
        super(ListSchedule.class);
    }
    @Override
    public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        int seed = num++;
        switch (seed) {
            //all starting with (x=0, start t1, start t2) = ([key], 0, 1)
            //all ending with (t1.join(),t2.join(),x.intValue()) = (0, 0, 0)
            case 2: return new ListSchedule(List.of(0,0,1, 0,0,0,0, 0,0,0)); //t1,t1,t2,t2
            case 1: return new ListSchedule(List.of(0,0,1, 1,1,0,0, 0,0,0)); //t2,t2,t1,t1
            case 0: return new ListSchedule(List.of(1,0,1, 0,1,0,0, 0,0,0)); //t1,t2,t1,t2
            case 3: return new ListSchedule(List.of(2,0,1, 1,0,1,0, 0,0,0)); //t2,t1,t2,t1
            default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0));
        }
    }
}