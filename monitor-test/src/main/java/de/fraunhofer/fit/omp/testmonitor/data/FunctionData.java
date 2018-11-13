package de.fraunhofer.fit.omp.testmonitor.data;

import de.fraunhofer.fit.omp.model.json.Assertion;
import de.fraunhofer.fit.omp.model.json.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
@RequiredArgsConstructor
public class FunctionData {
    @Nonnull final String ncname;
    @Nullable final QName inputElementName;
    @Nullable final QName outputElementName;
    @Nonnull final List<Assertion> assertions;

    public static FunctionData fromJSON(@Nonnull final Function json) {
        return new FunctionData(json.getNcname(), convert(json.getInputElementName()), convert(json.getOutputElementName()), json.getAssertions());
    }

    private static @Nullable
    QName convert(final @Nullable de.fraunhofer.fit.omp.model.json.QName qName) {
        if (null == qName || StringUtils.isEmpty(qName.getNcname())) {
            return null;
        }
        return new QName(qName.getNamespaceuri().toString(), qName.getNcname());
    }
}
