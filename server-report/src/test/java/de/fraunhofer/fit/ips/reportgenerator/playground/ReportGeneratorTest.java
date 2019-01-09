package de.fraunhofer.fit.ips.reportgenerator.playground;

import de.fraunhofer.fit.ips.reportgenerator.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled
@Slf4j
public class ReportGeneratorTest {

//    private static List<ReportType> testTypes = Arrays.asList(DOCX, PDF);
//
//    private static final String OUTPUT_DIR = "target/jsonTestOutput/";
//    private static final int NUM_OF_FILES = 10;
//    private static final int REPEAT_TESTS = 10;
//
//    private static ApplicationConfig config;
//    private static Application app;
//    private static CloseableHttpClient httpClient;
//
//    @BeforeAll
//    public static void initAll() {
//        config = ApplicationConfig.builder().serverLogRequests(false).build();
//
//        // make sure that the server is running
//        app = new Application(config);
//        app.start();
//
//        if (Files.notExists(Paths.get(OUTPUT_DIR))) {
//            boolean success = new File(OUTPUT_DIR).mkdirs();
//            if (!success) {
//                fail("Folder creation failed.");
//            }
//        }
//
//        httpClient = HttpClientBuilder.create().build();
//
//        log.info("Warming up the server...");
//        String jsonString = readFileOrThrow("src/test/resources/sampleJSON.json");
//        for (ReportType tt : testTypes) {
//            for (int i = 0; i < REPEAT_TESTS; i++) {
//                testDuration(createPost(jsonString, tt));
//            }
//        }
//        log.info("Warm-up finished.");
//    }
//
//    @AfterAll
//    public static void tearDownAll() {
//        try {
//            httpClient.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        app.stop();
//    }
//
//    @Test
//    public void useSentenceTest() {
//        executeTest("SENTENCE", 1, NUM_OF_FILES, 1);
//    }
//
//    @Test
//    public void useParagraphTest() {
//        executeTest("PARAGRAPH", 1, NUM_OF_FILES, 1);
//    }
//
//    @Test
//    public void useTextTest() {
//        executeTest("TEXT", 1, NUM_OF_FILES, 1);
//    }
//
//    @Test
//    public void BigTest() {
//        executeTest("SENTENCE", 10, 100, 10);
//        executeTest("PARAGRAPH", 10, 100, 10);
//        executeTest("TEXT", 10, 100, 10);
//    }
//
//    private static void executeTest(String docLengthType, int start, int end, int steps) {
//        for (ReportType tt : testTypes) {
//            for (int i = start; i <= end; i = i + steps) {
//
//                String jsonString = readFileOrThrow(docLengthType, i);
//
//                long totalTime = 0;
//                for (int j = 0; j < REPEAT_TESTS; j++) {
//                    totalTime += testDuration(createPost(jsonString, tt));
//                }
//                log.info("Average request time ({} {}, {}): {} ms", docLengthType, tt, i, (totalTime / REPEAT_TESTS));
//
//                // Download the file after duration tests, because otherwise duration tests will use (if caching is
//                // enabled) the cached report. Therefore, the measured duration would be misleading.
//                //
//                downloadFile(
//                        createPost(jsonString, tt),
//                        OUTPUT_DIR + "json_" + docLengthType + "_" + i + "." + tt.value
//                );
//            }
//        }
//    }
//
//    private static HttpPost createPost(String jsonString, ReportType tt) {
//        try {
//            HttpPost post = new HttpPost(new URIBuilder().setScheme("http")
//                                                         .setHost(config.getServerHost())
//                                                         .setPort(config.getServerPort())
//                                                         .setPath(config.getApiAsyncReportPath())
//                                                         .build());
//
//            post.setHeader(HttpHeaders.ACCEPT, tt.contentType);
//            post.setEntity(new StringEntity(jsonString, StandardCharsets.UTF_8));
//            post.setHeader("Content-type", "application/json");
//            return post;
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static long testDuration(HttpPost post) {
//        try {
//            long startTime = System.nanoTime();
//            return httpClient.execute(post, response -> {
//                checkStatus(response);
//
//                // If we do not have this line of code and skip to the next part (where we measure the time), we would
//                // be measuring the start of the transfer and not the end when the file (docx/pdf) is fully received,
//                // because the file is sent in chunked mode. Therefore, the measured duration would be misleading.
//                EntityUtils.consume(response.getEntity());
//
//                return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
//            });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static void downloadFile(HttpPost post, String filePath) {
//        try {
//            httpClient.execute(post, response -> {
//                checkStatus(response);
//                try (InputStream in = response.getEntity().getContent()) {
//                    File file = new File(filePath);
//                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                }
//                return null;
//            });
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static void checkStatus(HttpResponse response) {
//        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            throw new RuntimeException("Not OK, because: " + response.getStatusLine().getReasonPhrase());
//        }
//    }
//
//    private static String readFileOrThrow(String documentationLengthType, int iteration) {
//        return readFileOrThrow("src/test/resources/jsonTestFiles/json_" + documentationLengthType + "_" + iteration + ".json");
//    }
//
//    private static String readFileOrThrow(String path) {
//        try {
//            return Utils.readToString(new FileInputStream(new File(path)));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

}