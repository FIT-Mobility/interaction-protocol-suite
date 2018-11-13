package de.fraunhofer.fit.omp.reportgenerator.server;

import com.google.common.net.HttpHeaders;
import de.fraunhofer.fit.omp.reportgenerator.ReportType;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.11.2017
 */
public class ErrorMessages {

    public static final String INVALID_JSON = "Invalid JSON in post data";

    public static final String INVALID_XSD = "Invalid XML schema in post data";

    public static final String EXCEPTION_GENERAL = "Unknown error";

    public static final String CONTAINS_NO_REPORT_TYPE =
            "Unexpected '" + HttpHeaders.ACCEPT + "' header value for reporting. Please specify one of the following: " + ReportType.getContentTypesAsConcatString();

    public static final String REPORT_ID_NOT_SET = "The report id is not set";

    public static final String REPORT_NOT_FOUND = "The report is not found";

    public static final String MULTIPLE_TEMPLATES = "Multiple template ids are defined";

}
