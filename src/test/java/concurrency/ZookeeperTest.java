//package concurrency;
//
//import cmu.pasta.cdiff.Schedule;
//import com.pholser.junit.quickcheck.From;
//import edu.berkeley.cs.jqf.fuzz.Fuzz;
//import edu.berkeley.cs.jqf.fuzz.JQF;
//import org.apache.zookeeper.KeeperException;
//import org.junit.Ignore;
//import org.junit.runner.RunWith;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@RunWith(JQF.class)
//public class ZookeeperTest {
//    @Fuzz //@Ignore
//    public void testZookeeper(String hostPort, String znode, @From(RandomScheduleGenerator.class) Schedule s) throws InterruptedException {
//        Thread t = new Thread(() -> {
//            //perform test here
//            String filename = "tmp.txt";
//            String[] exec = new String[]{"echo", "hello"};
//            System.out.println("trying to make new ZookeeperExecutor with " + hostPort + ", " + znode);
//            try {
//                new ZookeeperExecutor(hostPort, znode, filename, exec).run();
//            } catch (KeeperException | IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        Map<String, Throwable> exceptions = new HashMap<>();
//        t.setUncaughtExceptionHandler((thread, e) -> exceptions.put(thread + " with:\nSchedule " + s + "\n", e));
//        t.start();
//        t.newJoin();
//        for(Map.Entry<String, Throwable> entry : exceptions.entrySet()) {
//            RuntimeException re = new RuntimeException(entry.getKey() + " threw " + entry.getValue());
//            re.setStackTrace(entry.getValue().getStackTrace());
//            System.out.println(re);
//            //throw re;
//        }
//    }
//}
