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
        ExecutionIndex t1Ind = new ExecutionIndex(new int[]{0, 3, -75489274, 1, -75489274, 2}), t2Ind = new ExecutionIndex(new int[]{0, 3, -75489273, 1, -75489273, 2});
        List<ExecutionIndex> list = new ArrayList<>(List.of(t2Ind, t2Ind, t1Ind, t1Ind, t1Ind
                //new ExecutionIndex(new int[]{0, 3, -75489273, 1, -75489273, 2})
        )); //do all the 74s and then the 73s to get non-violating
        list.add(new ExecutionIndex(new int[]{0, 0, 0, 0, 0, 0}));
        //list.add(new ExecutionIndex(new int[]{0, 0, 0, 0, 0, 0}));
        return new Schedule(list);
    } //TODO write integration tests for all four possibilities (-1<->0, 1<->0, 0 w/t1 first, 0 w/t2 first)
    //(freeze implementation to get reproducibility for ^)
    //TODO new schedule/schedulegenerator: list of indexes into sorted-known-threads when sorted-known-threads is sorted by execution index
}
