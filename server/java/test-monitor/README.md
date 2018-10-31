# Test-Monitor

This component contains a test-monitor able to validate message exchanges between to parties w.r.t. conformance to a protocol defined using this tool suite.
It supports different modes of operation described below.
The validation results are written to a PostgreSQL database (version 10 has been tested to work, 9 should be fine, too).
There is a rather strong binding between the database and the code.
Thus, to compile the code, one has to setup a database.
The database settings used during (the lifecycle phases up to) compilation are configured in the module pom.xml.
Using the default values, the database preparation might look like the following:

```$sql
CREATE USER jooq WITH PASSWORD 'jooq';
CREATE DATABASE "test-monitor" OWNER jooq;
```

Optionally, after connecting to the newly created database, you may want to execute `ALTER SCHEMA public OWNER TO jooq;`

The results written to the database were visualized using a Grafana dashboard for our purposes.
Database preparation for that should be more restrictive, e.g.:
```$sql
CREATE USER grafanareader WITH PASSWORD 'password';
GRANT USAGE ON SCHEMA public TO grafanareader;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO grafanareader;
```

An export of the dashboard developed to use with the VaaS mode of operation can be found under src/main/resources/grafana/vaas-dashboard.json

## Modes of Operation
The test-monitor currently offers four modes of operation, but was designed to be easily extensible:
- HTTP:
    This mode of operation puts the test-monitor between the two communicating parties as a man-in-the-middle.
    To use this mode, one has to configure a local port and a remote address.
    Messages received on the local port are validated and forwarded to the remote address.
    The answer received from the remote address is validated and in turn forwarded to the original sender.
    Furthermore, the two messages are combined to allow function level assertions.
    This setup makes it easy to correlate requests to their corresponding responses.
    In addition to the above, the protocol definition (as json) and the database setup has to be configured.
    Example config:
    ```$xml
    <Configuration xmlns="http://dimo.de/test-monitor-configuration">
        <HTTP name="http-test">
            <databaseConfig>
                <hostname>localhost</hostname>
                <port/>
                <dbname>test-monitor</dbname>
                <schema/>
                <user>jooq</user>
                <password>jooq</password>
            </databaseConfig>
            <jsonURI>http://127.0.0.1:8081/api/content/json/afbcec0d-cfc0-4720-8736-10c989c2a5a7</jsonURI>
            <localHost>0.0.0.0</localHost>
            <localPort>8080</localPort>
            <wsURI>http://localhost:9090</wsURI>
        </HTTP>
    </Configuration>
    ```
- VaaS:
    This mode of operation puts the test-monitor 'in the cloud'.
    One of the two communicating parties of the protocol has to send all incoming and outgoing messages to the test-monitor.
    This party also has to do the request-response matching.
    To use this mode, one just has to configure a local port for the test-monitor to listen on.
    Messages sent to this endpoint have to be enriched by the headers defined in the vaas-info module.
    One of these is the exchange identifier that allows the test-monitor to correlate requests and their corresponding responses.
    In addition to the above, the protocol definition (as json) and the database setup has to be configured.
    Example config:
    ```$xml
    <Configuration xmlns="http://dimo.de/test-monitor-configuration">
        <VAAS name="vaas-instance-9985">
            <databaseConfig>
                <hostname>localhost</hostname>
                <port/>
                <dbname>test-monitor</dbname>
                <schema/>
                <user>jooq</user>
                <password>jooq</password>
            </databaseConfig>
            <jsonURI>http://127.0.0.1:8081/api/content/json/afbcec0d-cfc0-4720-8736-10c989c2a5a7</jsonURI>
            <localHost>0.0.0.0</localHost>
            <localPort>9985</localPort>
        </VAAS>
    </Configuration>
    ```
- SOAP:
    This mode of operation is very similar to the HTTP mode, but allows for messages being wrapped within SOAP envelopes.
    The test-monitor will unwrap the messages before validation.
    Example config:
    ```$xml
    <Configuration xmlns="http://dimo.de/test-monitor-configuration">
        <SOAP name="soap-instance-1">
            <databaseConfig>
                <hostname>localhost</hostname>
                <port/>
                <dbname>test-monitor</dbname>
                <schema/>
                <user>jooq</user>
                <password>jooq</password>
            </databaseConfig>
            <jsonURI>http://127.0.0.1:8081/api/content/json/afbcec0d-cfc0-4720-8736-10c989c2a5a7</jsonURI>
            <localHost>0.0.0.0</localHost>
            <localPort>9980</localPort>
            <wsURI>http://localhost:9876</wsURI>
        </SOAP>
    </Configuration>
    ```
- MQTT:
    This mode of operation was designed for a side project and does not rely on protocols specified using this tool suite.
    Instead, it relies on the following:
    Functions can be identified by going through the (XSD) elements defined and looking for strings that appear once with a "Request" suffix and once with a "Response" suffix.
    Furthermore, topic validation constraints can be specified using the schema found below.
    These constraints are embedded within `xs:annotation/xs:appinfo` of top-level `xs:element`s.
    To use this mode, one has to configure the connection string for the broker, the directory containing the 'project' XSD files and the database setup.
    Example config:
    ```$xml
    <Configuration xmlns="http://dimo.de/test-monitor-configuration">
        <MQTT name="mqtt-instance-1">
            <databaseConfig>
                <hostname>localhost</hostname>
                <port/>
                <dbname>test-monitor</dbname>
                <schema/>
                <user>jooq</user>
                <password>jooq</password>
            </databaseConfig>
            <mqttBrokerHostString>tcp://127.0.0.1:1883&amp;connectWaitInSeconds=2592000</mqttBrokerHostString>
            <xsdDirectory>~/XSDs/FuH</xsdDirectory>
        </MQTT>
    </Configuration>
    ```

Within a runtime instance, an arbitrary number of modes of operation can be configured including the possibility to mix different modes and start several instances of one mode.


### Schema for Topic Validation in MQTT mode
```$xsd
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns="http://www.dimo-fuh.de/internal"
           targetNamespace="http://www.dimo-fuh.de/internal"
           elementFormDefault="qualified" version="2.0"
           xpathDefaultNamespace="##targetNamespace">

    <xs:complexType name="ValidTopicsStructure">
        <xs:annotation>
            <xs:documentation>
                Structure for storing a list of validTopics
            </xs:documentation>
        </xs:annotation>
        <xs:sequence>
            <xs:element name="ValidTopic" type="ValidTopicStructure" maxOccurs="unbounded">
                <xs:annotation>
                    <xs:documentation>
                        Valid Topic. May contain '+' for a single level wildcard and '#' for a multi-level wildcard.
                    </xs:documentation>
                </xs:annotation>
            </xs:element>
        </xs:sequence>
    </xs:complexType>

    <xs:complexType name="ValidTopicStructure">
        <xs:attribute name="topic" type="xs:normalizedString">
            <xs:annotation>
                <xs:documentation>
                    Valid Topic. May contain '+' for a single level wildcard and '#' for a multi-level wildcard.
                </xs:documentation>
            </xs:annotation>
        </xs:attribute>
    </xs:complexType>

    <xs:element name="ValidTopics" type="ValidTopicsStructure">
        <xs:annotation>
            <xs:documentation>
                Element for storing a list of validTopics
            </xs:documentation>
        </xs:annotation>
    </xs:element>
</xs:schema>
```
