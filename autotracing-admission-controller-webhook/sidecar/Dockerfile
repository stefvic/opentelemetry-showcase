FROM alpine:3

RUN wget -O opentelemetry-javaagent.jar "https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.2.0/opentelemetry-javaagent-1.2.0.jar"

RUN mkdir -p /mnt/shared

VOLUME /mnt/shared

ENTRYPOINT ["cp", "-v", "opentelemetry-javaagent.jar", "/mnt/shared"]
