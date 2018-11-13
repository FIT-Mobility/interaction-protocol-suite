package de.fraunhofer.fit.omp.reportgenerator.reporter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.fraunhofer.fit.omp.reportgenerator.ReportType;
import de.fraunhofer.fit.omp.reportgenerator.model.ReportWrapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.11.2017
 */
public class CachingReporter implements Reporter {

    private final Reporter delegate;
    private final HashFunction hashFunction;
    private final Cache<String, ReportWrapper> cache;

    public CachingReporter(Reporter delegate) {
        if (delegate instanceof CachingReporter) {
            throw new RuntimeException("delegate cannot be a CachingReporter instance");
        }

        this.delegate = delegate;
        this.hashFunction = Hashing.goodFastHash(128);
        this.cache = CacheBuilder.newBuilder()
                                 .maximumSize(100)
                                 .expireAfterAccess(30, TimeUnit.MINUTES)
                                 .build();
    }

    @Override
    public ReportType getType() {
        return delegate.getType();
    }

    @Override
    public ReportWrapper report(String templateId, String jsonAsString) throws Exception {
        HashCode hashCode = hashFunction.hashString(jsonAsString, StandardCharsets.UTF_8);
        String key = hashCode.toString();
        return cache.get(key, () -> delegate.report(templateId, jsonAsString));
    }

    public void invalidateCache() {
        cache.invalidateAll();
    }
}
