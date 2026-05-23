FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
COPY opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY src/main/resources/application.yml /app/config/application.yml
COPY src/main/resources/application-kubernetes.yml /app/config/application-kubernetes.yml
ENV OTEL_SERVICE_NAME=api-gateway
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4318
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar", "--spring.config.location=file:/app/config/"]

