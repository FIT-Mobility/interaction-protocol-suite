package de.fraunhofer.fit.ips.testmonitor.reporting;

import de.fraunhofer.fit.ips.testmonitor.configuration.MqttConfiguration;
import de.fraunhofer.fit.ips.testmonitor.Constants;
import jooq.testmonitor.tables.MqttErrorGeneralError;
import jooq.testmonitor.tables.MqttErrorIllegalTopic;
import jooq.testmonitor.tables.MqttErrorMissingFollowupMessage;
import jooq.testmonitor.tables.MqttErrorMissingFunctionInfo;
import jooq.testmonitor.tables.MqttErrorNoCorrelationId;
import jooq.testmonitor.tables.MqttInfoFunctionIdentified;
import jooq.testmonitor.tables.MqttInfoMessageReceived;
import jooq.testmonitor.tables.MqttInfoValidationStartFunction;
import jooq.testmonitor.tables.MqttInfoValidationStartMessage;
import jooq.testmonitor.tables.MqttWarningValidationIssue;
import org.apache.camel.component.mqtt.MQTTConfiguration;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import static jooq.testmonitor.tables.MqttCommonFields.MQTT_COMMON_FIELDS;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class MqttReporter extends ReporterBase {
    public MqttReporter(final MqttConfiguration configuration) {
        super(configuration,
                new Aspect<>(MQTT_COMMON_FIELDS.MQTT_TOPIC, message -> Objects.toString(message.getHeader(MQTTConfiguration.MQTT_SUBSCRIBE_TOPIC))),
                new Aspect<>(MQTT_COMMON_FIELDS.MQTT_CORRELATION_ID, message -> Objects.toString(message.getHeader(Constants.CORRELATION_ID)))
        );
    }

    private static final Map<TableWrapper<? extends Record>, TableImpl<? extends Record>> MAPPER = new IdentityHashMap<>();

    static {
        MAPPER.put(TableWrapper.TW_INFO_MESSAGE_RECEIVED, MqttInfoMessageReceived.MQTT_INFO_MESSAGE_RECEIVED);
        MAPPER.put(TableWrapper.TW_INFO_FUNCTION_IDENTIFIED, MqttInfoFunctionIdentified.MQTT_INFO_FUNCTION_IDENTIFIED);
        MAPPER.put(TableWrapper.TW_INFO_VALIDATION_START_MESSAGE, MqttInfoValidationStartMessage.MQTT_INFO_VALIDATION_START_MESSAGE);
        MAPPER.put(TableWrapper.TW_INFO_VALIDATION_START_FUNCTION, MqttInfoValidationStartFunction.MQTT_INFO_VALIDATION_START_FUNCTION);
        MAPPER.put(TableWrapper.TW_WARNING_VALIDATION_ISSUE, MqttWarningValidationIssue.MQTT_WARNING_VALIDATION_ISSUE);
        MAPPER.put(TableWrapper.TW_ERROR_MISSING_FUNCTION_INFO, MqttErrorMissingFunctionInfo.MQTT_ERROR_MISSING_FUNCTION_INFO);
        MAPPER.put(TableWrapper.TW_ERROR_MISSING_FOLLOWUP_MESSAGE, MqttErrorMissingFollowupMessage.MQTT_ERROR_MISSING_FOLLOWUP_MESSAGE);
        MAPPER.put(TableWrapper.TW_ERROR_GENERAL_ERROR, MqttErrorGeneralError.MQTT_ERROR_GENERAL_ERROR);
        // needs to be mapped since I implemented the logic in the base reporter
        MAPPER.put(TableWrapper.TW_MQTT_ERROR_ILLEGAL_TOPIC, MqttErrorIllegalTopic.MQTT_ERROR_ILLEGAL_TOPIC);
        MAPPER.put(TableWrapper.TW_MQTT_ERROR_NO_CORRELATION_ID, MqttErrorNoCorrelationId.MQTT_ERROR_NO_CORRELATION_ID);

        for (final TableWrapper<?> table : TableWrapper.TABLES) {
            final TableImpl<? extends Record> mapped = MAPPER.get(table);
            if (mapped == null) {
                throw new IllegalStateException("table " + table.getName() + " could not be mapped to its MQTT counterpart!");
            }
        }
    }

    @Override
    protected TableImpl<? extends Record> getTable(TableWrapper<? extends Record> table) {
        final TableImpl<? extends Record> mappedTable = MAPPER.get(table);
        if (null == mappedTable) {
            throw new IllegalStateException("table " + table.getName() + " could not be mapped to its MQTT counterpart!");
        }
        return mappedTable;
    }
}
