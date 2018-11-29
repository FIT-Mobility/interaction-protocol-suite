package de.fraunhofer.fit.ips.reportgenerator.model;

import de.fraunhofer.fit.ips.reportgenerator.ReportType;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2017
 */
@RequiredArgsConstructor
public class ReportWrapper {
    public final ReportType type;
    public final byte[] report;
    public final List<String> errors;

    public ReportWrapper(ReportType type, byte[] report) {
        this(type, report, Collections.emptyList());
    }
}
