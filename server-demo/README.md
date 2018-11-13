# Demo Server

This module contains a simple demonstration server that generates a web service skeleton based on a protocol specification developed using this tool suite.
Additionally, the test classes may easily resemble client logic.
Here, the communication basis is SOAP 1.2 which is added to the actual protocol specification by the omp-project2wsdl-maven-plugin module.
Therefore, the protocol project file (json) as well as the hostname to be published in the generated WSDL file have to be specified in the project pom.xml.
The web server server logic is augmented with a so-called interceptor that forwards all incoming and outgoing messages to a test-monitor running in VaaS mode.
To start the demo-server, one therefore has to configure the local endpoint to listen on as well as the validation endpoint to send the messages to.
For example, a configuration might look like this:
```$xml
<Configuration xmlns="http://dimo.de/demo-server-configuration">
    <localHostConfig>
        <hostname>0.0.0.0</hostname>
        <port>9876</port>
    </localHostConfig>
    <validationHost>http://127.0.0.1:9985</validationHost>
</Configuration>
```
Currently, the logic implemented fits the protocol specification located in `src/main/resources/plugfest-test.json`.
Also, in the file Application.java the address suffix `ShuttleService` is hard-coded.
This has to be adjusted accordingly in case of other protocol specifications (or just be implemented more generically).
The test classes create a local instance of the server. The address used there should match the address written into the WSDL file configured via the pom.xml.

# KNOWN ISSUES
Java 11 does not work with apache-cxf-codegen 3.2.6. The current snapshot of 3.3 already resolves this, update as soon as it is released.
