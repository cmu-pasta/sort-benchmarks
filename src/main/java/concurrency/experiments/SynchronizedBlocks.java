package concurrency.experiments;

public class SynchronizedBlocks {
    public volatile int x = 0;

    public void block1() {
        synchronized ("BLOCK") {
            System.out.println("I'm block 1!");
            x += 3;
            System.out.println("updated x to " + x);
        }
    }

    public void block2() {
        synchronized ("BLOCK") {
            System.out.println("I'm block 2!");
            x += x;
            System.out.println("updated x to " + x);
        }
    }
}
