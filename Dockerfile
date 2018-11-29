# using the magic from https://github.com/carlossg/docker-maven/issues/36#issuecomment-334562850
FROM adoptopenjdk/openjdk11:alpine-slim
RUN apk --no-cache --update add --virtual native-deps maven
WORKDIR /usr/src/ips
COPY . .
# RUN mvn -B -V -e -T 1C clean package -pl server-report -am
RUN mvn clean package -DskipTests -pl grpc-server -am -Djdk.tls.client.protocols="TLSv1,TLSv1.1,TLSv1.2"

# fyi for the mvn flags:
# -B     batch mode (non-interactive)
# -V     print version before building
# -e     produce execution error messages
# -C     strict checksums
# -T 1C  use 1.0 times number of cores
# -o     offline mode
# -pl    project list to be built
# -am    also make dependents

FROM adoptopenjdk/openjdk11:alpine-slim
COPY --from=0 /usr/src/ips/grpc-server/target/grpc-server.jar /opt/ips/

EXPOSE 50051
CMD java -jar /opt/ips/grpc-server.jar
