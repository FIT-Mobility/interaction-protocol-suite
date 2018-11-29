package de.fraunhofer.fit.ips.testmonitor.reporting;

import de.fraunhofer.fit.ips.testmonitor.configuration.ConfigurationBase;
import de.fraunhofer.fit.ips.vaas.VaasConstants;
import jooq.testmonitor.tables.MqttErrorIllegalTopic;
import jooq.testmonitor.tables.MqttErrorNoCorrelationId;
import jooq.testmonitor.tables.VaasErrorGeneralError;
import jooq.testmonitor.tables.VaasErrorMissingFollowupMessage;
import jooq.testmonitor.tables.VaasErrorMissingFunctionInfo;
import jooq.testmonitor.tables.VaasInfoFunctionIdentified;
import jooq.testmonitor.tables.VaasInfoMessageReceived;
import jooq.testmonitor.tables.VaasInfoValidationStartFunction;
import jooq.testmonitor.tables.VaasInfoValidationStartMessage;
import jooq.testmonitor.tables.VaasWarningValidationIssue;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import static jooq.testmonitor.tables.VaasCommonFields.VAAS_COMMON_FIELDS;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class VaasReporter extends ReporterBase {
    public VaasReporter(final ConfigurationBase configuration) {
        super(configuration,
                new Aspect<>(VAAS_COMMON_FIELDS.VAAS_FOREIGN_EXCHANGE_ID, message -> Objects.toString(message.getHeader(VaasConstants.HTTP_HEADER_EXCHANGE_ID))),
                new Aspect<>(VAAS_COMMON_FIELDS.VAAS_MESSAGE_TYPE, message -> Objects.toString(message.getHeader(VaasConstants.HTTP_HEADER_MESSAGE_TYPE)))
        );
    }

    private static final Map<TableWrapper<? extends Record>, TableImpl<? extends Record>> MAPPER = new IdentityHashMap<>();

    static {
        MAPPER.put(TableWrapper.TW_INFO_MESSAGE_RECEIVED, VaasInfoMessageReceived.VAAS_INFO_MESSAGE_RECEIVED);
        MAPPER.put(TableWrapper.TW_INFO_FUNCTION_IDENTIFIED, VaasInfoFunctionIdentified.VAAS_INFO_FUNCTION_IDENTIFIED);
        MAPPER.put(TableWrapper.TW_INFO_VALIDATION_START_MESSAGE, VaasInfoValidationStartMessage.VAAS_INFO_VALIDATION_START_MESSAGE);
        MAPPER.put(TableWrapper.TW_INFO_VALIDATION_START_FUNCTION, VaasInfoValidationStartFunction.VAAS_INFO_VALIDATION_START_FUNCTION);
        MAPPER.put(TableWrapper.TW_WARNING_VALIDATION_ISSUE, VaasWarningValidationIssue.VAAS_WARNING_VALIDATION_ISSUE);
        MAPPER.put(TableWrapper.TW_ERROR_MISSING_FUNCTION_INFO, VaasErrorMissingFunctionInfo.VAAS_ERROR_MISSING_FUNCTION_INFO);
        MAPPER.put(TableWrapper.TW_ERROR_MISSING_FOLLOWUP_MESSAGE, VaasErrorMissingFollowupMessage.VAAS_ERROR_MISSING_FOLLOWUP_MESSAGE);
        MAPPER.put(TableWrapper.TW_ERROR_GENERAL_ERROR, VaasErrorGeneralError.VAAS_ERROR_GENERAL_ERROR);
        // needs to be mapped since I implemented the logic in the base reporter
        MAPPER.put(TableWrapper.TW_MQTT_ERROR_ILLEGAL_TOPIC, MqttErrorIllegalTopic.MQTT_ERROR_ILLEGAL_TOPIC);
        MAPPER.put(TableWrapper.TW_MQTT_ERROR_NO_CORRELATION_ID, MqttErrorNoCorrelationId.MQTT_ERROR_NO_CORRELATION_ID);

        for (final TableWrapper<?> table : TableWrapper.TABLES) {
            final TableImpl<? extends Record> mapped = MAPPER.get(table);
            if (mapped == null) {
                throw new IllegalStateException("table " + table.getName() + " could not be mapped to its VaaS counterpart!");
            }
        }
    }

    @Override
    protected TableImpl<? extends Record> getTable(final TableWrapper<? extends Record> table) {
        final TableImpl<? extends Record> mappedTable = MAPPER.get(table);
        if (null == mappedTable) {
            throw new IllegalStateException("table " + table.getName() + " could not be mapped to its VaaS counterpart!");
        }
        return mappedTable;
    }
}
