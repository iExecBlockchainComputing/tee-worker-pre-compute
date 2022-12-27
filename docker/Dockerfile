## CI/CD version
## git clone, then cd into
## docker image build -f docker/Dockerfile -t nexus.intra.iex.ec/tee-worker-pre-compute:dev . --no-cache

###
### Build the final sconified image with
### the produced jar.
###
#
# Starting from here, the Sconification has been made using
# https://github.com/scontain/hello-world-java/blob/master/sconify.sh
# We then made slight updates:
# 1. Inject the right .jar file
# 2. Print the mrenclave at the end of the flow
#
FROM alpine:3.10
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
COPY build/libs/app-all.jar /app/app.jar
