package de.fraunhofer.fit.omp.testmonitor.reporting;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.fraunhofer.fit.omp.testmonitor.configuration.ConfigurationBase;
import de.fraunhofer.fit.omp.testmonitor.exception.IllegalTopicException;
import de.fraunhofer.fit.omp.testmonitor.exception.MissingFunctionInfoException;
import de.fraunhofer.fit.omp.testmonitor.exception.MonitorException;
import de.fraunhofer.fit.omp.testmonitor.exception.MonitorExceptionVisitor;
import de.fraunhofer.fit.omp.testmonitor.exception.NoCorrelationIdException;
import de.fraunhofer.fit.omp.testmonitor.exception.WrappingMonitorException;
import de.fraunhofer.fit.omp.testmonitor.routing.FunctionInfo;
import jooq.testmonitor.tables.records.ErrorGeneralErrorRecord;
import jooq.testmonitor.tables.records.ErrorMissingFollowupMessageRecord;
import jooq.testmonitor.tables.records.ErrorMissingFunctionInfoRecord;
import jooq.testmonitor.tables.records.InfoFunctionIdentifiedRecord;
import jooq.testmonitor.tables.records.InfoMessageReceivedRecord;
import jooq.testmonitor.tables.records.InfoValidationStartFunctionRecord;
import jooq.testmonitor.tables.records.InfoValidationStartMessageRecord;
import jooq.testmonitor.tables.records.MqttErrorIllegalTopicRecord;
import jooq.testmonitor.tables.records.MqttErrorNoCorrelationIdRecord;
import jooq.testmonitor.tables.records.WarningValidationIssueRecord;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.TableField;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.TableImpl;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static jooq.testmonitor.tables.CommonFields.COMMON_FIELDS;
import static jooq.testmonitor.tables.ErrorGeneralError.ERROR_GENERAL_ERROR;
import static jooq.testmonitor.tables.ErrorMissingFollowupMessage.ERROR_MISSING_FOLLOWUP_MESSAGE;
import static jooq.testmonitor.tables.ErrorMissingFunctionInfo.ERROR_MISSING_FUNCTION_INFO;
import static jooq.testmonitor.tables.InfoFunctionIdentified.INFO_FUNCTION_IDENTIFIED;
import static jooq.testmonitor.tables.InfoMessageReceived.INFO_MESSAGE_RECEIVED;
import static jooq.testmonitor.tables.InfoValidationStartFunction.INFO_VALIDATION_START_FUNCTION;
import static jooq.testmonitor.tables.InfoValidationStartMessage.INFO_VALIDATION_START_MESSAGE;
import static jooq.testmonitor.tables.MqttErrorIllegalTopic.MQTT_ERROR_ILLEGAL_TOPIC;
import static jooq.testmonitor.tables.MqttErrorNoCorrelationId.MQTT_ERROR_NO_CORRELATION_ID;
import static jooq.testmonitor.tables.WarningValidationIssue.WARNING_VALIDATION_ISSUE;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
@Getter
public class ReporterBase implements Reporter, AutoCloseable {
    protected final String configurationName;

    protected final ImmutableList<Aspect<?>> aspects;

    protected final DSLContext dslContext;

    private final HikariDataSource dataSource;

    public ReporterBase(final ConfigurationBase configuration, final Aspect<?>... furtherAspects) {
        this.dataSource = initDataSource(configuration.getDatabaseConfig());
        this.dslContext = DSL.using(new DefaultConfiguration()
                .set(SQLDialect.POSTGRES)
                .set(new DataSourceConnectionProvider(dataSource))
                .set(new Settings().withExecuteLogging(true)));

        this.configurationName = configuration.getName();
        final String runIdentifier = UUID.randomUUID().toString();
        this.aspects = ImmutableList.<Aspect<?>>builder()
                .add(new Aspect<>(COMMON_FIELDS.CONFIG_NAME, m -> configurationName))
                .add(new Aspect<>(COMMON_FIELDS.RUN_ID, m -> runIdentifier))
                .add(new Aspect<>(COMMON_FIELDS.ROUTE_ID, m -> Optional.ofNullable(m.getExchange()).map(Exchange::getFromRouteId).orElse(null)))
                .add(new Aspect<>(COMMON_FIELDS.EXCHANGE_ID, m -> Optional.ofNullable(m.getExchange()).map(Exchange::getExchangeId).orElse(null)))
                .add(new Aspect<>(COMMON_FIELDS.MESSAGE_ID, Message::getMessageId))
                .add(furtherAspects)
                .build();
    }

    private static HikariDataSource initDataSource(final ConfigurationBase.DatabaseConfig dbConfig) {
        final HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(org.postgresql.ds.PGSimpleDataSource.class.getName());

        config.addDataSourceProperty("serverName", dbConfig.getHostname());
        config.addDataSourceProperty("portNumber", dbConfig.getPort());
        config.addDataSourceProperty("databaseName", dbConfig.getDbname());
        config.addDataSourceProperty("user", dbConfig.getUser());
        config.addDataSourceProperty("password", dbConfig.getPassword());
        config.addDataSourceProperty("currentSchema", dbConfig.getSchema());

        return new HikariDataSource(config);
    }

    public static class TableWrapper<R extends Record> {
        private final TableImpl<R> table;
        static List<TableWrapper<?>> TABLES = new ArrayList<>();

        public TableWrapper(final TableImpl<R> table) {
            this.table = table;
            TABLES.add(this);
        }

        public String getName() {
            return table.getName();
        }

        static final TableWrapper<InfoMessageReceivedRecord> TW_INFO_MESSAGE_RECEIVED = new TableWrapper<>(INFO_MESSAGE_RECEIVED);
        static final TableWrapper<InfoFunctionIdentifiedRecord> TW_INFO_FUNCTION_IDENTIFIED = new TableWrapper<>(INFO_FUNCTION_IDENTIFIED);
        static final TableWrapper<InfoValidationStartMessageRecord> TW_INFO_VALIDATION_START_MESSAGE = new TableWrapper<>(INFO_VALIDATION_START_MESSAGE);
        static final TableWrapper<InfoValidationStartFunctionRecord> TW_INFO_VALIDATION_START_FUNCTION = new TableWrapper<>(INFO_VALIDATION_START_FUNCTION);
        static final TableWrapper<WarningValidationIssueRecord> TW_WARNING_VALIDATION_ISSUE = new TableWrapper<>(WARNING_VALIDATION_ISSUE);
        static final TableWrapper<ErrorGeneralErrorRecord> TW_ERROR_GENERAL_ERROR = new TableWrapper<>(ERROR_GENERAL_ERROR);
        static final TableWrapper<ErrorMissingFollowupMessageRecord> TW_ERROR_MISSING_FOLLOWUP_MESSAGE = new TableWrapper<>(ERROR_MISSING_FOLLOWUP_MESSAGE);
        static final TableWrapper<ErrorMissingFunctionInfoRecord> TW_ERROR_MISSING_FUNCTION_INFO = new TableWrapper<>(ERROR_MISSING_FUNCTION_INFO);
        static final TableWrapper<MqttErrorNoCorrelationIdRecord> TW_MQTT_ERROR_NO_CORRELATION_ID = new TableWrapper<>(MQTT_ERROR_NO_CORRELATION_ID);
        static final TableWrapper<MqttErrorIllegalTopicRecord> TW_MQTT_ERROR_ILLEGAL_TOPIC = new TableWrapper<>(MQTT_ERROR_ILLEGAL_TOPIC);
    }

    private static <R extends Record, T> InsertSetMoreStep<R> typeSafeSetter(final InsertSetStep<R> insertInto,
                                                                             final Aspect<T> aspect,
                                                                             final Message message) {
        return insertInto.set(aspect.getKey(), aspect.getValue(message));
    }

    private <R extends Record> InsertSetMoreStep<R> insertAspects(final Message message,
                                                                  final InsertSetStep<R> insertInto) {
        InsertSetMoreStep<R> returnValue = null;
        for (final Aspect<?> aspect : aspects) {
            returnValue = typeSafeSetter(insertInto, aspect, message);
        }
        return returnValue;
    }

    @RequiredArgsConstructor(staticName = "of")
    static class FieldValue<R extends Record, T> {
        final TableField<R, T> field;
        final T value;
    }

    private static <T> void typeSafeSetter(final InsertSetMoreStep<? extends Record> insertInto,
                                           final FieldValue<? extends Record, T> field) {
        insertInto.set(field.field, field.value);
    }

    @SafeVarargs
    final synchronized protected <R extends Record> void write(final Message message,
                                                               final TableWrapper<R> table,
                                                               final FieldValue<R, ?>... fields) {
        final InsertSetStep<? extends Record> insertInto = dslContext.insertInto(getTable(table));
        final InsertSetMoreStep<? extends Record> aspectsInserted = insertAspects(message, insertInto);
        for (final FieldValue<R, ?> field : fields) {
            typeSafeSetter(aspectsInserted, field);
        }
        aspectsInserted.execute();
    }

    protected TableImpl<? extends Record> getTable(final TableWrapper<? extends Record> table) {
        return table.table;
    }

    @Override
    public void messageReceived(final Exchange exchange) {
        final Message message = exchange.getMessage();
        write(message, TableWrapper.TW_INFO_MESSAGE_RECEIVED,
                FieldValue.of(INFO_MESSAGE_RECEIVED.MESSAGE_BODY, message.getBody(String.class))
        );
    }

    @Override
    public void processFunctionIdentified(final Message message, final FunctionInfo functionInfo) {
        write(message, TableWrapper.TW_INFO_FUNCTION_IDENTIFIED,
                FieldValue.of(INFO_FUNCTION_IDENTIFIED.FUNCTION_NAME, functionInfo.getFunctionName()),
                FieldValue.of(INFO_FUNCTION_IDENTIFIED.SERVICE_NAME, functionInfo.getServiceName())
        );
    }

    @Override
    public void processStartOfMessageValidation(final Message message, final String validationContent,
                                                final ValidationTarget validationTarget) {
        write(message, TableWrapper.TW_INFO_VALIDATION_START_MESSAGE,
                FieldValue.of(INFO_VALIDATION_START_MESSAGE.VALIDATION_CONTENT, validationContent),
                FieldValue.of(INFO_VALIDATION_START_MESSAGE.VALIDATION_TARGET, validationTarget.getJooq())
        );
    }

    @Override
    public void processStartOfFunctionValidation(final Message functionMessage,
                                                 final Message request,
                                                 final Message response) {
        write(functionMessage, TableWrapper.TW_INFO_VALIDATION_START_FUNCTION,
                FieldValue.of(INFO_VALIDATION_START_FUNCTION.VALIDATION_CONTENT, functionMessage.getBody(String.class)),
                FieldValue.of(INFO_VALIDATION_START_FUNCTION.REQUEST_MESSAGE_ID, request.getMessageId()),
                FieldValue.of(INFO_VALIDATION_START_FUNCTION.RESPONSE_MESSAGE_ID, response.getMessageId())
        );
    }

    @Override
    public void processValidationError(final Message message,
                                       final SAXParseException exception) {
        write(message, TableWrapper.TW_WARNING_VALIDATION_ISSUE,
                FieldValue.of(WARNING_VALIDATION_ISSUE.WARNING_TYPE, exception.getClass().getName()),
                FieldValue.of(WARNING_VALIDATION_ISSUE.WARNING_LINE, exception.getLineNumber()),
                FieldValue.of(WARNING_VALIDATION_ISSUE.WARNING_COLUMN, exception.getColumnNumber()),
                FieldValue.of(WARNING_VALIDATION_ISSUE.WARNING_MESSAGE, exception.getLocalizedMessage()),
                FieldValue.of(WARNING_VALIDATION_ISSUE.VALIDATED_PUBLIC_ID, exception.getPublicId()),
                FieldValue.of(WARNING_VALIDATION_ISSUE.VALIDATED_SYSTEM_ID, exception.getSystemId())
        );
    }

    @Override
    public void processMissingFunctionInfo(final Message message, final String lookupKey) {
        write(message, TableWrapper.TW_ERROR_MISSING_FUNCTION_INFO,
                FieldValue.of(ERROR_MISSING_FUNCTION_INFO.ERROR_TYPE, "missing function info"),
                FieldValue.of(ERROR_MISSING_FUNCTION_INFO.ERROR_CLASS_NAME, "/"),
                FieldValue.of(ERROR_MISSING_FUNCTION_INFO.LOOKUP_KEY, lookupKey)
        );
    }

    @Override
    public void processMissingFollowUpMessage(final Message message, final String missingElementName,
                                              final int timeOutAfterInSeconds) {
        write(message, TableWrapper.TW_ERROR_MISSING_FOLLOWUP_MESSAGE,
                FieldValue.of(ERROR_MISSING_FOLLOWUP_MESSAGE.ERROR_TYPE, "missing function info"),
                FieldValue.of(ERROR_MISSING_FOLLOWUP_MESSAGE.ERROR_CLASS_NAME, "/"),
                FieldValue.of(ERROR_MISSING_FOLLOWUP_MESSAGE.MISSING_ELEMENT_NAME, missingElementName),
                FieldValue.of(ERROR_MISSING_FOLLOWUP_MESSAGE.TIMEOUT_AFTER_SECONDS, timeOutAfterInSeconds)
        );
    }

    @Override
    public void processWarning(final MonitorException exception) {
        exception.accept(new MonitorExceptionVisitor() {
            @Override
            public void visit(final IllegalTopicException illegalTopicException) {
                write(illegalTopicException.getAffectedMessage(), TableWrapper.TW_MQTT_ERROR_ILLEGAL_TOPIC,
                        FieldValue.of(MQTT_ERROR_ILLEGAL_TOPIC.ERROR_TYPE, "illegal topic"),
                        FieldValue.of(MQTT_ERROR_ILLEGAL_TOPIC.ERROR_CLASS_NAME, illegalTopicException.getClass().getName()),
                        FieldValue.of(MQTT_ERROR_ILLEGAL_TOPIC.ILLEGAL_TOPIC, illegalTopicException.getIllegalTopic()),
                        FieldValue.of(MQTT_ERROR_ILLEGAL_TOPIC.LEGAL_TOPICS, Objects.toString(illegalTopicException.getLegalTopics()))
                );
            }

            @Override
            public void visit(final MissingFunctionInfoException missingFunctionInfoException) {
                write(missingFunctionInfoException.getAffectedMessage(), TableWrapper.TW_ERROR_MISSING_FUNCTION_INFO,
                        FieldValue.of(ERROR_MISSING_FUNCTION_INFO.ERROR_TYPE, "missing function info (err)"),
                        FieldValue.of(ERROR_MISSING_FUNCTION_INFO.ERROR_CLASS_NAME, missingFunctionInfoException.getClass().getName()),
                        FieldValue.of(ERROR_MISSING_FUNCTION_INFO.LOOKUP_KEY, missingFunctionInfoException.getElementName().toString())
                );
            }

            @Override
            public void visit(final NoCorrelationIdException noCorrelationIdException) {
                write(noCorrelationIdException.getAffectedMessage(), TableWrapper.TW_MQTT_ERROR_NO_CORRELATION_ID,
                        FieldValue.of(MQTT_ERROR_NO_CORRELATION_ID.ERROR_TYPE, "no correlation id found"),
                        FieldValue.of(MQTT_ERROR_NO_CORRELATION_ID.ERROR_CLASS_NAME, noCorrelationIdException.getClass().getName()),
                        FieldValue.of(MQTT_ERROR_NO_CORRELATION_ID.PROBLEMATIC_TOPIC, noCorrelationIdException.getTopic())
                );
            }

            @Override
            public void visit(final WrappingMonitorException wrappingMonitorException) {
                write(wrappingMonitorException.getAffectedMessage(), TableWrapper.TW_ERROR_GENERAL_ERROR,
                        FieldValue.of(ERROR_GENERAL_ERROR.ERROR_TYPE, "wrapped exception"),
                        FieldValue.of(ERROR_GENERAL_ERROR.ERROR_CLASS_NAME, wrappingMonitorException.getWrapped().getClass().getName()),
                        FieldValue.of(ERROR_GENERAL_ERROR.STACK_TRACE, ExceptionUtils.getStackTrace(wrappingMonitorException.getWrapped()))
                );
            }
        });
    }

    @Override
    public void onException(final Exchange exchange) {
        final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        if (exception instanceof MonitorException) {
            processWarning((MonitorException) exception);
        } else {
            write(exchange.getMessage(), TableWrapper.TW_ERROR_GENERAL_ERROR,
                    FieldValue.of(ERROR_GENERAL_ERROR.ERROR_TYPE, "exception in route"),
                    FieldValue.of(ERROR_GENERAL_ERROR.ERROR_CLASS_NAME, exception.getClass().getName()),
                    FieldValue.of(ERROR_GENERAL_ERROR.STACK_TRACE, ExceptionUtils.getStackTrace(exception))
            );
        }
    }

    @Override
    public synchronized void close() {
        dataSource.close();
    }
}
