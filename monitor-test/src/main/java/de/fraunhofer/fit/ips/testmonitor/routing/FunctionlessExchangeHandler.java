package de.fraunhofer.fit.ips.testmonitor.routing;

import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

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
