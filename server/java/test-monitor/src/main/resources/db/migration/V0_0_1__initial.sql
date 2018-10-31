-- general tables

CREATE TABLE common_fields
(
  id SERIAL NOT NULL,
  event_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  config_name TEXT,
  run_id TEXT,
  route_id TEXT,
  exchange_id TEXT,
  message_id TEXT,
  CONSTRAINT pk_common_fields_key PRIMARY KEY (id)
);

CREATE TABLE informational_messages
(
) INHERITS (common_fields);

CREATE TABLE warnings
(
  warning_type TEXT
) INHERITS (common_fields);

CREATE TABLE errors
(
  error_type TEXT,
  error_class_name TEXT
) INHERITS (common_fields);

CREATE TYPE validation_target_enum AS ENUM ('request', 'response');

-- informational messages

CREATE TABLE info_message_received
(
  message_body TEXT
) INHERITS (informational_messages);

CREATE TABLE info_function_identified
(
  function_name TEXT,
  service_name TEXT
) INHERITS (informational_messages);

CREATE TABLE info_validation_start
(
  validation_content TEXT
) INHERITS (informational_messages);

CREATE TABLE info_validation_start_message
(
  validation_target validation_target_enum
) INHERITS (info_validation_start);

CREATE TABLE info_validation_start_function
(
  request_message_id TEXT,
  response_message_id TEXT
) INHERITS (info_validation_start);

-- warnings

CREATE TABLE warning_validation_issue
(
  warning_line integer,
  warning_column integer,
  warning_message TEXT,
  validated_public_id TEXT,
  validated_system_id TEXT
) INHERITS (warnings);

-- errors

CREATE TABLE error_missing_function_info
(
  lookup_key TEXT
) INHERITS (errors);

CREATE TABLE error_missing_followup_message
(
  missing_element_name TEXT,
  timeout_after_seconds integer
) INHERITS (errors);

CREATE TABLE error_general_error
(
  stack_trace TEXT
) INHERITS (errors);


---------------------
-- MQTT EXTENSIONS --
---------------------

-- general tables

CREATE TABLE mqtt_common_fields
(
  mqtt_topic TEXT,
  mqtt_correlation_id TEXT
) INHERITS (common_fields);

CREATE TABLE mqtt_informational_messages
(
) INHERITS (mqtt_common_fields, informational_messages);

CREATE TABLE mqtt_warnings
(
) INHERITS (mqtt_common_fields, warnings);

CREATE TABLE mqtt_errors
(
) INHERITS (mqtt_common_fields, errors);

-- informational messages

CREATE TABLE mqtt_info_message_received
(
) INHERITS (mqtt_informational_messages, info_message_received);

CREATE TABLE mqtt_info_function_identified
(
) INHERITS (mqtt_informational_messages, info_function_identified);

CREATE TABLE mqtt_info_validation_start
(
) INHERITS (mqtt_informational_messages, info_validation_start);

CREATE TABLE mqtt_info_validation_start_message
(
) INHERITS (mqtt_info_validation_start, info_validation_start_message);

CREATE TABLE mqtt_info_validation_start_function
(
) INHERITS (mqtt_info_validation_start, info_validation_start_function);

-- warnings

CREATE TABLE mqtt_warning_validation_issue
(
) INHERITS (mqtt_warnings, warning_validation_issue);

-- errors

CREATE TABLE mqtt_error_missing_function_info
(
) INHERITS (mqtt_errors, error_missing_function_info);

CREATE TABLE mqtt_error_missing_followup_message
(
) INHERITS (mqtt_errors, error_missing_followup_message);

CREATE TABLE mqtt_error_general_error
(
) INHERITS (mqtt_errors, error_general_error);

CREATE TABLE mqtt_error_illegal_topic
(
  illegal_topic TEXT,
  legal_topics TEXT
) INHERITS (mqtt_errors);

CREATE TABLE mqtt_error_no_correlation_id
(
  problematic_topic TEXT
) INHERITS (mqtt_errors);


---------------------
-- VAAS EXTENSIONS --
---------------------

-- general tables

CREATE TABLE vaas_common_fields
(
  vaas_foreign_exchange_id TEXT,
  vaas_message_type TEXT
) INHERITS (common_fields);

CREATE TABLE vaas_informational_messages
(
) INHERITS (vaas_common_fields, informational_messages);

CREATE TABLE vaas_warnings
(
) INHERITS (vaas_common_fields, warnings);

CREATE TABLE vaas_errors
(
) INHERITS (vaas_common_fields, errors);

-- informational messages

CREATE TABLE vaas_info_message_received
(
) INHERITS (vaas_informational_messages, info_message_received);

CREATE TABLE vaas_info_function_identified
(
) INHERITS (vaas_informational_messages, info_function_identified);

CREATE TABLE vaas_info_validation_start
(
) INHERITS (vaas_informational_messages, info_validation_start);

CREATE TABLE vaas_info_validation_start_message
(
) INHERITS (vaas_info_validation_start, info_validation_start_message);

CREATE TABLE vaas_info_validation_start_function
(
) INHERITS (vaas_info_validation_start, info_validation_start_function);

-- warnings

CREATE TABLE vaas_warning_validation_issue
(
) INHERITS (vaas_warnings, warning_validation_issue);

-- errors

CREATE TABLE vaas_error_missing_function_info
(
) INHERITS (vaas_errors, error_missing_function_info);

CREATE TABLE vaas_error_missing_followup_message
(
) INHERITS (vaas_errors, error_missing_followup_message);

CREATE TABLE vaas_error_general_error
(
) INHERITS (vaas_errors, error_general_error);

