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
                new ExecutionIndex(new int[]{0, 3, -469753850, 2, -469753850, 2}),
                new ExecutionIndex(new int[]{0, 3, -469753849, 2, -469753849, 2})
        ));
        Collections.reverse(list);
        return new Schedule(list);
    }
}
