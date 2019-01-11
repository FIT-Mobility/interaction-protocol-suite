# Interaction Protocol Specification And Validation Tool Suite

This project comprises a set of tools supporting specification, documentation, development and validation of interaction protocols and their implementations.

## Building and starting the application 
1. Preparation of the environment settings: Adjust .env files in `server-js` and `frontend` folders.
2. Build and start the application using Docker and docker-compose via `docker-compose build && docker-compose up`
3. Navigate to http://localhost:8081 to view the page

## Production usage
Start the server with `docker-compose -f docker-compose.yml -f docker-compose.prod.yml build && docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d` instead.
This will merge the production config into the dev config and enable the SSL-serving frontend.
You may need to make sure your ssl key files match the expected names, see `./proxy/nginx.conf`.

## structural overview
![structural overview graphics](https://g.gravizo.com/source/svg?https%3A%2F%2Fraw.githubusercontent.com%2FFIT-Mobility%2Finteraction-protocol-suite%2Fmaster%2Fstructural-overview.gv)

Solid lines stand for build dependencies, dotted lines depict interaction at runtime.
Orange nodes represent components written in Java, blue ones are written in TypeScript.

The tool suite consists of three main logical units:
- The specification web tool: The entry point is the frontend component, which needs the server-js component to work, which in turn relies on the functionality of the grpc-server written in Java.
- The test monitor: The Java component monitor-test can be used to validate interactions wrt conformance to specified protocols.
- The demo server: The Java component server-demo contains a client and server skeleton based on a protocol specification, which can easily be adapted to any protocol specified using the web tool.

The rest of the modules are auxiliary modules.

## Notes

### PDF and Fonts

For the PDF preview that is part of the web tool, the report-generator module needs the fonts used in the template to generate the PDF.
Since our template makes use of proprietary fonts that we can't bundle with our application for legal reasons, we have provided several mechanisms to make fonts available to the module.
- system dependent mechanisms: the most common directories fonts are installed to are scanned for fonts
- the server/data/fonts directory is mounted to /root/.fonts for docker-setups. You may place fonts into this directory to have them detected. You should, however, check that no legal issues prohibit you to do so.
- fonts with more permissive licenses were added as fallbacks for common proprietary fonts, such as Calibri

## User Data

User-generated data is automatically saved in `./data`. E.g. mongodb writes to `./data/mongo` and users upload their files to `./data/uploads`.
This also means that while the containers can be created and destroyed at any time, `./data` must persist and must be secured.

### Build Tools

To build the Typescript modules, we use yarn (https://yarnpkg.com)
To build the Java modules, we use maven (https://maven.apache.org).

We are using lombok (https://projectlombok.org/) in many of the Java modules to auto-generate some dumb code (like getters and setters) during compilation.
Maven already understands this and can work with this. If your IDE cannot and claims that the methods are missing, install the lombok plugin for your IDE.

- For IntelliJ: https://plugins.jetbrains.com/plugin/6317-lombok-plugin
- For Eclipse: https://projectlombok.org/setup/eclipse
- Others: https://projectlombok.org/setup/overview
