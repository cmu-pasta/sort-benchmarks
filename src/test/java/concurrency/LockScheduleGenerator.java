package concurrency;

import cmu.pasta.cdiff.schedule.ListSchedule;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.List;

public class LockScheduleGenerator extends Generator<ListSchedule> {
    public static int num = 0;

    public LockScheduleGenerator() {
        super(ListSchedule.class);
    }
    @Override
    public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        int seed = num++;
        switch (seed) {
            //all starting with (start t1, start t2) = ([key], [not key])
            //all ending with (t1.join(),t2.join()) = (0, 0)
            case 0: return new ListSchedule(List.of(0,1, 0,0,0,0,0,0, 0,0)); //t1,t1,t1,t2,t2,t2
            case 1: return new ListSchedule(List.of(0,1, 1,1,1,0,0,0, 0,0)); //t2,t2,t2,t1,t1,t1
            case 2: return new ListSchedule(List.of(1,0, 0,0,1,0,0,0, 0,0)); //t1,t1,t2,t1,t2,t2
            case 3: return new ListSchedule(List.of(1,0, 0,0,1,1,0,0, 0,0)); //t1,t1,t2,t2,t1,t2
            case 4: return new ListSchedule(List.of(2,1, 0,0,1,1,1,0, 0,0)); //t1,t1,t2,t2,t2,t1
            case 5: return new ListSchedule(List.of(1,0, 0,1,0,0,1,0, 0,0)); //t1,t2,t1,t1,t2,t2
            case 6: return new ListSchedule(List.of(1,0, 0,1,0,1,0,0, 0,0)); //t1,t2,t1,t2,t1,t2
            case 7: return new ListSchedule(List.of(2,1, 0,1,0,1,1,0, 0,0)); //t1,t2,t1,t2,t2,t1
            case 8: return new ListSchedule(List.of(1,0, 0,1,1,0,0,0, 0,0)); //t1,t2,t2,t1,t1,t2
            case 9: return new ListSchedule(List.of(2,1, 0,1,1,0,1,0, 0,0)); //t1,t2,t2,t1,t2,t1
            case 10: return new ListSchedule(List.of(2,1, 0,1,1,1,0,0, 0,0)); //t1,t2,t2,t2,t1,t1
            case 11: return new ListSchedule(List.of(2,1, 1,1,0,1,0,0, 0,0)); //t2,t2,t1,t2,t1,t1
            case 12: return new ListSchedule(List.of(2,1, 1,1,0,0,1,0, 0,0)); //t2,t2,t1,t1,t2,t1
            case 13: return new ListSchedule(List.of(1,0, 1,1,0,0,0,0, 0,0)); //t2,t2,t1,t1,t1,t2
            case 14: return new ListSchedule(List.of(2,1, 1,0,1,1,0,0, 0,0)); //t2,t1,t2,t2,t1,t1
            case 15: return new ListSchedule(List.of(2,1, 1,0,1,0,1,0, 0,0)); //t2,t1,t2,t1,t2,t1
            case 16: return new ListSchedule(List.of(1,0, 1,0,1,0,0,0, 0,0)); //t2,t1,t2,t1,t1,t2
            case 17: return new ListSchedule(List.of(2,1, 1,0,0,1,1,0, 0,0)); //t2,t1,t1,t2,t2,t1
            case 18: return new ListSchedule(List.of(1,0, 1,0,0,1,0,0, 0,0)); //t2,t1,t1,t2,t1,t2
            case 19: return new ListSchedule(List.of(1,0, 1,0,0,0,0,0, 0,0)); //t2,t1,t1,t1,t2,t2
            default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0));
        }
    }
}