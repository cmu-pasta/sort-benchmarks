# Sort benchmarks

This repository contains various sorting algorithms extracted from the web. TimSort is the default sorting implementation in OpenJDK when calling `Collections.sort()`.

The various test methods in `sort.SortTest` take in exactly the same formal parameters. This is so that we can run a fuzzer on one of those methods, and then replay on another sorting method. This is useful to study cross-target coverage and mutation scores.

## Build

```
mvn package
```

## Fuzzing

Let's run fuzzing on Timsort for 10 seconds:
```
mvn jqf:fuzz -Dclass=sort.SortTest -Dmethod=testTimSort -Dout=fuzz-results -Dtime=10s
```

The fuzzer-generated input corpus should be stored in `target/fuzz-results/corpus`.

## Repro

There are multiple ways to replay the executions for different purposes.

1. Repro single method with JQF and print integers

```
mvn jqf:repro  -Dclass=sort.SortTest -Dmethod=testTimSort -DprintArgs -Dinput=target/fuzz-results/corpus/
```

You can also give a specific input file name in `-Dinput` if you want to run only one input

2. Maven test (the repro destination is hard-coded in a static field in `sort.SortTest`; change this if your directory is different)
```
mvn test -Dtest=sort.SortTest#testTimSort
```

### Repro with coverage

3. Maven test with coverage on command-line

There are three phases to run: (1) `prepare-agent` instruments the classes to collect coverage, (2) `test` runs the test and logs coverage data in `jacoco.exec`, (3) `report` converts the logged exec data into HTML

```
rm -f target/jacoco.exec # Remove old exec
mvn jacoco:prepare-agent test -Dtest=sort.SortTest#testTimSort jacoco:report
```

Open results as HTML: `target/site/jacoco/index.html`

4. Run in your IDE with coverage

In IntelliJ IDEA, click on the green arrow in the left margin and choose "Run with coverage". You can do this either for the whole class `sort.SortTest` or individual test methods.

