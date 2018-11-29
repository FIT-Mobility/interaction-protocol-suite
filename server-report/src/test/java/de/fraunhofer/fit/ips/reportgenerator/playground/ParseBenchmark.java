package de.fraunhofer.fit.ips.reportgenerator.playground;

import de.fraunhofer.fit.ips.reportgenerator.Application;
import de.fraunhofer.fit.ips.reportgenerator.ApplicationConfig;
import de.fraunhofer.fit.ips.reportgenerator.converter.JsonDataConverter3;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.SyntaxKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 17.11.2017
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ParseBenchmark {

    private String json;
    private IXDocReport report;
    private JsonDataConverter3 modelConverter3;

    /**
     * Main method to run benchmark
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParseBenchmark.class.getSimpleName())
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
                                    .getResource("jsonTestFiles/json_TEXT_100.json")
                                    .toURI())
                    ), StandardCharsets.UTF_8.name()
            );
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String template = ApplicationConfig.builder().build().getReportingDocxTemplate();

        try (InputStream in = Application.class.getClassLoader()
                                               .getResourceAsStream(template)) {

            XDocReportRegistry registry = XDocReportRegistry.getRegistry();
            report = registry.loadReport(in, TemplateEngineKind.Velocity);

            FieldsMetadata metadata = new FieldsMetadata();
            metadata.addFieldAsTextStyling("project.documentation", SyntaxKind.Html);
            metadata.addFieldAsTextStyling("service.documentation", SyntaxKind.Html);
            metadata.addFieldAsTextStyling("sequence.documentation", SyntaxKind.Html);
            metadata.addFieldAsTextStyling("function.documentation", SyntaxKind.Html);
            metadata.addFieldAsTextStyling("datatype.documentation", SyntaxKind.Html);

            report.setFieldsMetadata(metadata);
            modelConverter3 = new JsonDataConverter3();

        } catch (IOException | XDocReportException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void toContext3Bench(Blackhole bh) throws Exception {
        IContext ctx = modelConverter3.getContext(report, json);
        bh.consume(ctx);
    }
}
