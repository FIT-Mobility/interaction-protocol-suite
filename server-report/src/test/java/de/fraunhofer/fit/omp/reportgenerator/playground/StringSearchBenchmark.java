package de.fraunhofer.fit.omp.reportgenerator.playground;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.02.2018
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringSearchBenchmark {

    private String json;
    private Pattern pattern;

    /**
     * Main method to run benchmark
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StringSearchBenchmark.class.getSimpleName())
                //.addProfiler(GCProfiler.class)
                .warmupIterations(10)
                .measurementIterations(10)
                .forks(1)
                .threads(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public final void prepare() {
        try {
            json = new String(
                    Files.readAllBytes(
                            Paths.get(ConversionFromFileToFile.class
                                    .getClassLoader()
                                    .getResource("sampleJSON2.json")
                                    .toURI())
                    ), StandardCharsets.UTF_8.name()
            );
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        pattern = Pattern.compile("(.*)(<ol>|<ul>)(.*)");
    }

    @Benchmark
    public void stringContains(Blackhole bh) {
        boolean contains = json.contains("<ul>");
        if (!contains) {
            contains = json.contains("<ol>");
        }
        bh.consume(contains);
    }

    @Benchmark
    public void regexPattern(Blackhole bh) {
        boolean matches = pattern.matcher(json).matches();
        bh.consume(matches);
    }
}
