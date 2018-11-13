package de.fraunhofer.fit.omp.testmonitor.routing;

import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
import org.apache.camel.Message;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class FunctionlessExchangeHandler extends ExchangeHandler<FunctionInfo> {
    public FunctionlessExchangeHandler(@Nonnull final InstanceValidator dataTypeValidator,
                                       @Nonnull final InnerBodyExtractor innerBodyExtractor) {
        super(dataTypeValidator, new FunctionInfo() {
            @Override
            public MEP getMep() {
                return MEP.Message;
            }

            @Override
            public QName getFunctionElementName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return "unidentified";
            }

            @Override
            public String getServiceName() {
                return "unidentified";
            }
        }, innerBodyExtractor);
    }
}
