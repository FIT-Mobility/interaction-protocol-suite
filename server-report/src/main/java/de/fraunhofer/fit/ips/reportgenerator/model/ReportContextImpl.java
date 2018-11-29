package de.fraunhofer.fit.ips.reportgenerator.model;

import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Schema;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.velocity.internal.XDocVelocityContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.01.2018
 */
@RequiredArgsConstructor
public class ReportContextImpl extends XDocVelocityContext implements ReportContext {
    private final IContext delegate;
    @Getter
    private final Schema schema;

    // -------------------------------------------------------------------------
    // From XDocVelocityContext
    // -------------------------------------------------------------------------

    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    public Object get(String key) {
        return delegate.get(key);
    }

    @Override
    public void putMap(Map<String, Object> contextMap) {
        delegate.putMap(contextMap);
    }

    @Override
    public Map<String, Object> getContextMap() {
        return delegate.getContextMap();
    }
}
