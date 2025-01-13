FROM eclipse-temurin:17.0.13_11-jre-focal

ARG jar

RUN test -n "$jar"

# By default JVM will try to allocate 16GB heap, let's reduce its size a bit
ENV JAVA_TOOL_OPTIONS="-Xmx256m"

COPY $jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
