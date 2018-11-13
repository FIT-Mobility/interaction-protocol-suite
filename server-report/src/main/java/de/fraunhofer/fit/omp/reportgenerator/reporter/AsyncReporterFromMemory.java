package de.fraunhofer.fit.omp.reportgenerator.reporter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.fraunhofer.fit.omp.reportgenerator.ReportType;
import de.fraunhofer.fit.omp.reportgenerator.model.ReportWrapper;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Report type agnostic async reporter class
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.11.2017
 */
public class AsyncReporterFromMemory implements AsyncReporter {

    private final Reporter delegate;
    private final Cache<String, ReportWrapper> cache;

    public AsyncReporterFromMemory(Reporter delegate) {
        if (delegate instanceof AsyncReporter) {
            throw new RuntimeException("delegate cannot be a AsyncReporter instance");
        }

        this.delegate = delegate;
        this.cache = CacheBuilder.newBuilder()
                                 .maximumSize(200)
                                 .expireAfterAccess(1, TimeUnit.HOURS)
                                 .build();
    }

    @Override
    public ReportType getType() {
        return delegate.getType();
    }

    @Nullable
    @Override
    public ReportWrapper get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public String generate(String templateId, String jsonAsString) throws Exception {
        ReportWrapper rw = delegate.report(templateId, jsonAsString);
        String key = UUID.randomUUID().toString();
        cache.put(key, rw);
        return key;
    }
}
