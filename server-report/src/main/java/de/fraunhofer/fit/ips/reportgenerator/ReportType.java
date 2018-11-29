package de.fraunhofer.fit.ips.reportgenerator;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.11.2017
 */
public enum ReportType {

    // https://technet.microsoft.com/en-us/library/ee309278(office.12).aspx
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("pdf", "application/pdf");

    public final String value;
    public final String contentType;

    ReportType(String value, String contentType) {
        this.value = value;
        this.contentType = contentType;
    }

    public static String getContentTypesAsConcatString() {
        ReportType[] enums = ReportType.values();
        int enumCount = enums.length;

        StringBuilder b = new StringBuilder();
        if (enumCount > 0) {
            b.append(enums[0].contentType);
            for (int i = 1; i < enumCount; i++) {
                b.append(", ").append(enums[i].contentType);
            }
        }
        return b.toString();
    }

    public static ReportType fromContentType(String contentType) {
        for (ReportType reportType : ReportType.values()) {
            if (reportType.contentType.equals(contentType)) {
                return reportType;
            }
        }
        throw new RuntimeException("contentType not found");
    }
}
