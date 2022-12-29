FROM alpine:3.10

ARG jar

ENV LANG C.UTF-8

# By default JVM will try to allocate 16GB heap, let's reduce its size a bit
ENV JAVA_TOOL_OPTIONS="-Xmx256m"

# This is necessary to prevent java from execve'ing itself
ENV LD_LIBRARY_PATH=/usr/lib/jvm/java-11-openjdk/lib/server:/usr/lib/jvm/java-11-openjdk/lib:/usr/lib/jvm/java-11-openjdk/../lib

# add a simple script that can auto-detect the appropriate JAVA_HOME value
# based on whether the JDK or only the JRE is installed
RUN { \
    echo '#!/bin/sh'; \
        echo 'set -e'; \
        echo; \
        echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
    } > /usr/local/bin/docker-java-home \
    && chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-11-openjdk/jre/bin:/usr/lib/jvm/java-11-openjdk/bin
RUN apk add --no-cache openjdk11 \
    && [ "$JAVA_HOME" = "$(docker-java-home)" ]
RUN which java

COPY $jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
