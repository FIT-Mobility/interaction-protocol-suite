# using the magic from https://github.com/carlossg/docker-maven/issues/36#issuecomment-334562850
FROM maven:3-jdk-10-slim
WORKDIR /usr/src/omp/server/java
COPY . .
RUN mvn -B -V -e -T 1C clean package -pl report-generator -am

# fyi for the mvn flags:
# -B     batch mode (non-interactive)
# -V     print version before building
# -e     produce execution error messages
# -C     strict checksums
# -T 1C  use 1.0 times number of cores
# -o     offline mode
# -pl    project list to be built
# -am    also make dependents

FROM openjdk:10-slim
COPY --from=0 /usr/src/omp/server/java/report-generator/target/lib /opt/omp/lib
COPY --from=0 /usr/src/omp/server/java/report-generator/target/omp-report-generator.jar /opt/omp/omp-report-generator.jar

EXPOSE 8080
CMD java -jar /opt/omp/omp-report-generator.jar
