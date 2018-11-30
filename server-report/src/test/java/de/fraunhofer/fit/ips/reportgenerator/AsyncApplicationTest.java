package de.fraunhofer.fit.ips.reportgenerator;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.11.2017
 */
public class AsyncApplicationTest {

//    private static String json;
//
//    private ApplicationConfig config;
//    private Application app;
//
//    private final BiFunction<ReportType, HttpResponse, String> idHandler = (reportType, response) -> {
//        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
//        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString());
//
//        // process body
//        try (InputStream content = response.getEntity().getContent()) {
//            JsonNode object = JsonObjectMapper.INSTANCE.get().readTree(Utils.readToString(content));
//            assertEquals(Boolean.TRUE, object.get("success").asBoolean());
//            JsonNode reportId = object.get(reportType.contentType);
//            return reportId.asText();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    };
//
//    private final ResponseHandler<Object> pdfResponseHandler = response -> {
//        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
//        checkContentHeaders(response, ReportType.PDF);
//        new PdfReader(response.getEntity().getContent()).close();
//        return null;
//    };
//
//    private final ResponseHandler<Object> docxResponseHandler = response -> {
//        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
//        checkContentHeaders(response, ReportType.DOCX);
//        XDocArchive.readZip(response.getEntity().getContent()).dispose();
//        return null;
//    };
//
//    @BeforeAll
//    public static void init() {
//        try {
//            json = new String(
//                    Files.readAllBytes(
//                            Paths.get(AsyncApplicationTest.class.getClassLoader()
//                                                                .getResource("sampleJSON2.json")
//                                                                .toURI())
//                    ), StandardCharsets.UTF_8.name()
//            );
//        } catch (IOException | URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    public void noCaching() throws Exception {
//        config = ApplicationConfig.builder()
//                                  .serverLogRequests(false)
//                                  .build();
//        app = new Application(config);
//        try {
//            app.start();
//            testAllCases();
//        } finally {
//            app.stop();
//        }
//    }
//
//    @Test
//    public void withCache() throws Exception {
//        config = ApplicationConfig.builder()
//                                  .serverLogRequests(false)
//                                  .reportingCacheReports(true)
//                                  .build();
//        app = new Application(config);
//        try {
//            app.start();
//            testAllCases();
//        } finally {
//            app.stop();
//        }
//    }
//
//    private void testAllCases() throws Exception {
//        testDocx();
//        testPdf();
//        testAllReportTypes();
//
//        testInvalidPath();
//        testUnexpectedReportType();
//        testWrongBody();
//        testNonExistingReport();
//    }
//
//    private void testAllReportTypes() throws Exception {
//        List<String> reportContentTypes = Stream.of(ReportType.values())
//                                                .map(it -> it.contentType)
//                                                .collect(Collectors.toList());
//
//        Map<String, String> reportIdMap = executePost(json, reportContentTypes, response -> {
//            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
//            assertHeaderValue(response, HttpHeaders.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString());
//
//            // process body
//            try (InputStream content = response.getEntity().getContent()) {
//                JsonNode object = JsonObjectMapper.INSTANCE.get().readTree(Utils.readToString(content));
//                assertEquals(Boolean.TRUE, object.get("success").asBoolean());
//
//                Map<String, String> map = new HashMap<>();
//                for (String contentType : reportContentTypes) {
//                    String reportId = object.get(contentType).asText();
//                    map.put(contentType, reportId);
//                }
//                return map;
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        for (Map.Entry<String, String> entry : reportIdMap.entrySet()) {
//            String path = config.getApiAsyncReportPath() + "/" + entry.getValue();
//            executeGet(entry.getValue(), response -> {
//                assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
//                Header[] headers = response.getHeaders(HttpHeaders.CONTENT_TYPE);
//                assertEquals(1, headers.length);
//                ReportType rt = ReportType.fromContentType(headers[0].getValue());
//                switch (rt) {
//                    case DOCX:
//                        XDocArchive.readZip(response.getEntity().getContent()).dispose();
//                        break;
//                    case PDF:
//                        new PdfReader(response.getEntity().getContent()).close();
//                        break;
//                    default:
//                        throw new RuntimeException("Unexpected ReportType");
//                }
//                return null;
//            }, path);
//        }
//    }
//
//    private void testNonExistingReport() throws Exception {
//        String pdfGetPath = config.getApiAsyncReportPath() + "/" + UUID.randomUUID().toString();
//        executeGet(ReportType.PDF.contentType, response -> {
//            assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
//            return "";
//        }, pdfGetPath);
//    }
//
//    private void testPdf() throws Exception {
//        String reportId = executePost(json, Collections.singletonList(ReportType.PDF.contentType), response -> idHandler.apply(ReportType.PDF, response));
//
//        String pdfGetPath = config.getApiAsyncReportPath() + "/" + reportId;
//        executeGet(ReportType.PDF.contentType, pdfResponseHandler, pdfGetPath);
//    }
//
//    private void testDocx() throws Exception {
//        String reportId = executePost(json, Collections.singletonList(ReportType.DOCX.contentType), response -> idHandler.apply(ReportType.DOCX, response));
//
//        String pdfGetPath = config.getApiAsyncReportPath() + "/" + reportId;
//        executeGet(ReportType.DOCX.contentType, docxResponseHandler, pdfGetPath);
//    }
//
//    private void testInvalidPath() throws Exception {
//        List<String> paths = Arrays.asList(
//                config.getApiAsyncReportPath(),
//                config.getApiAsyncReportPath() + "/"
//        );
//
//        for (String path : paths) {
//            executeGet(ReportType.PDF.contentType, response -> {
//                assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
//                return null;
//            }, path);
//        }
//
//        String notFoundPath = config.getApiAsyncReportPath() + "/" + UUID.randomUUID().toString();
//        executeGet(ReportType.PDF.contentType, response -> {
//            assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
//            return null;
//        }, notFoundPath);
//    }
//
//    private void testUnexpectedReportType() throws Exception {
//        executePost(json, Collections.singletonList("lol"), this::handleInvalidResponse);
//    }
//
//    private void testWrongBody() throws Exception {
//        executePost("this is not a json", Collections.singletonList(ReportType.PDF.contentType), this::handleInvalidResponse);
//    }
//
//    // -------------------------------------------------------------------------
//    // Private helpers
//    // -------------------------------------------------------------------------
//
//    private <T> T executePost(String body, List<String> reportMimeTypes, ResponseHandler<T> responseHandler)
//            throws Exception {
//        HttpClient client = HttpClientBuilder.create().build();
//
//        HttpPost post = new HttpPost(new URIBuilder().setScheme("http")
//                                                     .setHost(config.getServerHost())
//                                                     .setPort(config.getServerPort())
//                                                     .setPath(config.getApiAsyncReportPath())
//                                                     .build());
//        post.setEntity(new StringEntity(body));
//
//        // if everything goes ok
//        reportMimeTypes.forEach(type -> post.addHeader(HttpHeaders.ACCEPT, type));
//        // in case of errors, server should return a json body
//        post.addHeader(HttpHeaders.ACCEPT, MimeTypes.Type.APPLICATION_JSON.asString());
//
//        return client.execute(post, responseHandler);
//    }
//
//    private <T> T executeGet(String mimeTypeForReport, ResponseHandler<T> responseHandler, String path)
//            throws Exception {
//        HttpClient client = HttpClientBuilder.create().build();
//
//        HttpGet get = new HttpGet(new URIBuilder().setScheme("http")
//                                                  .setHost(config.getServerHost())
//                                                  .setPort(config.getServerPort())
//                                                  .setPath(path)
//                                                  .build());
//        // if everything goes ok
//        get.addHeader(HttpHeaders.ACCEPT, mimeTypeForReport);
//        // in case of errors, server should return a json body
//        get.addHeader(HttpHeaders.ACCEPT, MimeTypes.Type.APPLICATION_JSON.asString());
//
//        return client.execute(get, responseHandler);
//    }
//
//    private String handleInvalidResponse(HttpResponse response) {
//        // check headers
//        int statusCode = response.getStatusLine().getStatusCode();
//        assertEquals(HttpStatus.SC_BAD_REQUEST, statusCode);
//        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString());
//        assertHeaderValue(response, HttpHeaders.CONTENT_DISPOSITION, "inline");
//
//        // check body
//        try (InputStream content = response.getEntity().getContent()) {
//            JsonNode object = JsonObjectMapper.INSTANCE.get().readTree(Utils.readToString(content));
//            assertEquals(Boolean.FALSE, object.get("success").asBoolean());
//            assertNotNull(object.get("msg"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return null;
//    }
//
//    private static void assertHeaderValue(HttpResponse resp, String name, String value) {
//        Header[] headers = resp.getHeaders(name);
//        assertEquals(1, headers.length);
//        assertTrue(headers[0].getValue().startsWith(value));
//    }
//
//    private static void checkContentHeaders(HttpResponse response, ReportType rt) {
//        assertHeaderValue(response, HttpHeaders.CONTENT_TYPE, rt.contentType);
//    }
}
