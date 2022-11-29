package concurrency;

import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.ei.ExecutionIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleGenerator extends Generator<Schedule> {
    public ScheduleGenerator() {
        super(Schedule.class);
    }

    @Override
    public Schedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
        //TODO this is specific to AtomicityViolationTest
        List<ExecutionIndex> list = new ArrayList<>(List.of(
                new ExecutionIndex(new int[]{0, 3, -75489274, 1, -75489274, 2}),
                new ExecutionIndex(new int[]{0, 3, -75489273, 1, -75489273, 2}),
                new ExecutionIndex(new int[]{0, 3, -75489274, 1, -75489274, 2}),
                new ExecutionIndex(new int[]{0, 3, -75489273, 1, -75489273, 2})
                //new ExecutionIndex(new int[]{0, 3, -75489273, 1, -75489273, 2})
        )); //do all the 74s and then the 73s to get non-violating
        list.add(new ExecutionIndex(new int[]{0, 0, 0, 0, 0, 0}));
        list.add(new ExecutionIndex(new int[]{0, 0, 0, 0, 0, 0}));
        return new Schedule(list);
    }
}
