CREATE OR REPLACE VIEW exchanges_all AS
  SELECT DISTINCT common_fields.exchange_id,
                  common_fields.config_name,
                  common_fields.run_id,
                  min(common_fields.event_time) as event_time
  FROM common_fields
  GROUP BY common_fields.exchange_id,
           common_fields.config_name,
           common_fields.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW exchanges_success AS
  SELECT DISTINCT informational_messages.exchange_id,
                  informational_messages.config_name,
                  informational_messages.run_id,
                  MIN(informational_messages.event_time) as event_time
  FROM informational_messages
         INNER JOIN (SELECT DISTINCT informational_messages.exchange_id
                     FROM informational_messages
                     EXCEPT
                     (SELECT DISTINCT warnings.exchange_id FROM warnings
                      UNION DISTINCT
                      SELECT DISTINCT errors.exchange_id FROM errors)) as error_free
           ON informational_messages.exchange_id = error_free.exchange_id
  GROUP BY informational_messages.exchange_id, informational_messages.config_name,
           informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW exchanges_warnings AS
  SELECT DISTINCT informational_messages.exchange_id,
                  informational_messages.config_name,
                  informational_messages.run_id,
                  min(informational_messages.event_time) as event_time
  FROM informational_messages
         INNER JOIN (SELECT DISTINCT warnings.exchange_id FROM warnings
                     EXCEPT
                     SELECT DISTINCT errors.exchange_id FROM errors) as warnings
           ON informational_messages.exchange_id = warnings.exchange_id
  GROUP BY informational_messages.exchange_id, informational_messages.config_name,
           informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW exchanges_errors AS
  SELECT DISTINCT informational_messages.exchange_id,
                  informational_messages.config_name,
                  informational_messages.run_id,
                  min(informational_messages.event_time) as event_time
  FROM informational_messages
         INNER JOIN (SELECT DISTINCT errors.exchange_id FROM errors) as errors
           ON informational_messages.exchange_id = errors.exchange_id
  GROUP BY informational_messages.exchange_id, informational_messages.config_name,
           informational_messages.run_id
  ORDER BY event_time;


CREATE OR REPLACE VIEW messages_all AS
  SELECT DISTINCT common_fields.message_id, min(common_fields.event_time) as event_time
  FROM common_fields
  WHERE message_id NOT LIKE '%-function'
  GROUP BY common_fields.message_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW messages_success AS
  SELECT DISTINCT informational_messages.message_id,
                  informational_messages.config_name,
                  informational_messages.run_id,
                  min(informational_messages.event_time) as event_time
  FROM informational_messages
         INNER JOIN (SELECT DISTINCT informational_messages.message_id FROM informational_messages
                     EXCEPT
                     (SELECT DISTINCT warnings.message_id FROM warnings
                      UNION DISTINCT
                      SELECT DISTINCT errors.message_id FROM errors)) as error_free
           ON informational_messages.message_id = error_free.message_id
  WHERE informational_messages.message_id NOT LIKE '%-function'
  GROUP BY informational_messages.message_id,
           informational_messages.config_name,
           informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW messages_warnings AS
  SELECT DISTINCT informational_messages.message_id,
                  informational_messages.config_name,
                  informational_messages.run_id,
                  min(informational_messages.event_time) as event_time
  FROM informational_messages
         INNER JOIN (SELECT DISTINCT warnings.message_id FROM warnings
                     EXCEPT
                     SELECT DISTINCT errors.message_id FROM errors) as warnings
           ON informational_messages.message_id = warnings.message_id
  WHERE informational_messages.message_id NOT LIKE '%-function'
  GROUP BY informational_messages.message_id,
           informational_messages.config_name,
           informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW messages_errors AS
  SELECT DISTINCT informational_messages.message_id,
                  informational_messages.config_name,
                  informational_messages.run_id,
                  min(informational_messages.event_time) as event_time
  FROM informational_messages
         INNER JOIN (SELECT DISTINCT errors.message_id FROM errors) as errors
           ON informational_messages.message_id = errors.message_id
  WHERE informational_messages.message_id NOT LIKE '%-function'
  GROUP BY informational_messages.message_id,
           informational_messages.config_name,
           informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW all_validation_starts AS
  SELECT vsm.event_time,
         vsm.config_name,
         vsm.run_id,
         vsm.exchange_id,
         vsm.message_id,
         vsm.validation_target :: text,
         vsm.validation_content
  FROM info_validation_start_message as vsm
  UNION ALL
  SELECT vsf.event_time,
         vsf.config_name,
         vsf.run_id,
         vsf.exchange_id,
         vsf.message_id,
         'function' as validation_target,
         vsf.validation_content
  FROM info_validation_start_function as vsf
  ORDER BY event_time;




---------------------
-- VAAS EXTENSIONS --
---------------------

CREATE OR REPLACE VIEW vaas_exchanges_all AS
  SELECT DISTINCT vaas_common_fields.vaas_foreign_exchange_id,
                  vaas_common_fields.config_name,
                  vaas_common_fields.run_id,
                  min(vaas_common_fields.event_time) as event_time
  FROM vaas_common_fields
  GROUP BY vaas_common_fields.vaas_foreign_exchange_id,
           vaas_common_fields.config_name,
           vaas_common_fields.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_exchanges_success AS
  SELECT DISTINCT vaas_informational_messages.vaas_foreign_exchange_id,
                  vaas_informational_messages.config_name,
                  vaas_informational_messages.run_id,
                  MIN(vaas_informational_messages.event_time) as event_time
  FROM vaas_informational_messages
         INNER JOIN (SELECT DISTINCT vaas_informational_messages.vaas_foreign_exchange_id
                     FROM vaas_informational_messages
                     EXCEPT
                     (SELECT DISTINCT vaas_warnings.vaas_foreign_exchange_id FROM vaas_warnings
                      UNION DISTINCT
                      SELECT DISTINCT vaas_errors.vaas_foreign_exchange_id FROM vaas_errors)) as error_free
           ON vaas_informational_messages.vaas_foreign_exchange_id = error_free.vaas_foreign_exchange_id
  GROUP BY vaas_informational_messages.vaas_foreign_exchange_id, vaas_informational_messages.config_name,
           vaas_informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_exchanges_warnings AS
  SELECT DISTINCT vaas_informational_messages.vaas_foreign_exchange_id,
                  vaas_informational_messages.config_name,
                  vaas_informational_messages.run_id,
                  min(vaas_informational_messages.event_time) as event_time
  FROM vaas_informational_messages
         INNER JOIN (SELECT DISTINCT vaas_warnings.vaas_foreign_exchange_id FROM vaas_warnings
                     EXCEPT
                     SELECT DISTINCT vaas_errors.vaas_foreign_exchange_id FROM vaas_errors) as warnings
           ON vaas_informational_messages.vaas_foreign_exchange_id = warnings.vaas_foreign_exchange_id
  GROUP BY vaas_informational_messages.vaas_foreign_exchange_id, vaas_informational_messages.config_name,
           vaas_informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_exchanges_errors AS
  SELECT DISTINCT vaas_informational_messages.vaas_foreign_exchange_id,
                  vaas_informational_messages.config_name,
                  vaas_informational_messages.run_id,
                  min(vaas_informational_messages.event_time) as event_time
  FROM vaas_informational_messages
         INNER JOIN (SELECT DISTINCT vaas_errors.vaas_foreign_exchange_id FROM vaas_errors) as errors
           ON vaas_informational_messages.vaas_foreign_exchange_id = errors.vaas_foreign_exchange_id
  GROUP BY vaas_informational_messages.vaas_foreign_exchange_id, vaas_informational_messages.config_name,
           vaas_informational_messages.run_id
  ORDER BY event_time;


CREATE OR REPLACE VIEW vaas_messages_all AS
  SELECT DISTINCT vaas_common_fields.message_id, min(vaas_common_fields.event_time) as event_time
  FROM vaas_common_fields
  WHERE message_id NOT LIKE '%-function'
  GROUP BY vaas_common_fields.message_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_messages_success AS
  SELECT DISTINCT vaas_informational_messages.message_id,
                  vaas_informational_messages.config_name,
                  vaas_informational_messages.run_id,
                  min(vaas_informational_messages.event_time) as event_time
  FROM vaas_informational_messages
         INNER JOIN (SELECT DISTINCT vaas_informational_messages.message_id FROM vaas_informational_messages
                     EXCEPT
                     (SELECT DISTINCT vaas_warnings.message_id FROM vaas_warnings
                      UNION DISTINCT
                      SELECT DISTINCT vaas_errors.message_id FROM vaas_errors)) as error_free
           ON vaas_informational_messages.message_id = error_free.message_id
  WHERE vaas_informational_messages.message_id NOT LIKE '%-function'
  GROUP BY vaas_informational_messages.message_id,
           vaas_informational_messages.config_name,
           vaas_informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_messages_warnings AS
  SELECT DISTINCT vaas_informational_messages.message_id,
                  vaas_informational_messages.config_name,
                  vaas_informational_messages.run_id,
                  min(vaas_informational_messages.event_time) as event_time
  FROM vaas_informational_messages
         INNER JOIN (SELECT DISTINCT vaas_warnings.message_id FROM vaas_warnings
                     EXCEPT
                     SELECT DISTINCT vaas_errors.message_id FROM vaas_errors) as warnings
           ON vaas_informational_messages.message_id = warnings.message_id
  WHERE vaas_informational_messages.message_id NOT LIKE '%-function'
  GROUP BY vaas_informational_messages.message_id,
           vaas_informational_messages.config_name,
           vaas_informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_messages_errors AS
  SELECT DISTINCT vaas_informational_messages.message_id,
                  vaas_informational_messages.config_name,
                  vaas_informational_messages.run_id,
                  min(vaas_informational_messages.event_time) as event_time
  FROM vaas_informational_messages
         INNER JOIN (SELECT DISTINCT vaas_errors.message_id FROM vaas_errors) as errors
           ON vaas_informational_messages.message_id = errors.message_id
  WHERE vaas_informational_messages.message_id NOT LIKE '%-function'
  GROUP BY vaas_informational_messages.message_id,
           vaas_informational_messages.config_name,
           vaas_informational_messages.run_id
  ORDER BY event_time;

CREATE OR REPLACE VIEW vaas_all_validation_starts AS
  SELECT vsm.event_time,
         vsm.config_name,
         vsm.run_id,
         vsm.vaas_foreign_exchange_id,
         vsm.exchange_id,
         vsm.message_id,
         vsm.validation_target :: text,
         vsm.validation_content
  FROM vaas_info_validation_start_message as vsm
  UNION ALL
  SELECT vsf.event_time,
         vsf.config_name,
         vsf.run_id,
         vsf.vaas_foreign_exchange_id,
         vsf.exchange_id,
         vsf.message_id,
         'function' as validation_target,
         vsf.validation_content
  FROM vaas_info_validation_start_function as vsf
  ORDER BY event_time;
