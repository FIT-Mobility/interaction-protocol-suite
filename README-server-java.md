# DiMo Tool Suite

This folder contains a set of Java modules supporting protocol specification, documentation and validation.
The main modules are:
   - report-generator: Java component serving as backend for the protocol specification web tool
   - test-monitor: Java component used to validate interactions wrt conformance to specified protocols
   - demo-server: Java client and server skeleton based on a protocol specification

The rest of the modules are auxiliary modules.

To build the modules, use maven (https://maven.apache.org).

### Notes

We are using lombok (https://projectlombok.org/) to auto-generate some dumb code (like getters and setters) during compilation.
Maven already understands this and can work with this. If your IDE cannot and claims that the methods are missing, install the lombok plugin for your IDE.

- For IntelliJ: https://plugins.jetbrains.com/plugin/6317-lombok-plugin
- For Eclipse: https://projectlombok.org/setup/eclipse
- Others: https://projectlombok.org/setup/overview
