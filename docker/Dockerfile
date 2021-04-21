## CI/CD version
## git clone, then cd into
## docker image build -f docker/Dockerfile -t nexus.iex.ec/tee-worker-pre-compute:dev . --no-cache

ARG HOME=/home
ARG JAR_PATH=$HOME/build/libs/app-all.jar

###
### Build application jar
###
FROM gradle:6.8.3-jdk11 as gradle-builder
ARG HOME
WORKDIR $HOME
COPY build.gradle gradle.properties settings.gradle  ./
COPY src/ src/
RUN gradle build -i --no-daemon

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
FROM alpine:3.10 as native
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
ARG JAR_PATH
COPY --from=gradle-builder $JAR_PATH /app/app.jar

FROM registry.scontain.com:5050/sconecuratedimages/iexec-sconify-image:5.3.1 AS cli
COPY --from=native /usr/lib/jvm/java-11-openjdk/bin/java /usr/lib/jvm/java-11-openjdk/bin/java
RUN sconify.sh /usr/lib/jvm/java-11-openjdk/bin/java

FROM registry.scontain.com:5050/sconecuratedimages/iexec-sconify-image:5.3.1 AS crosscompiler
RUN apk add --no-cache patchelf
COPY --from=native /app /app
COPY --from=native /lib/ /lib/
COPY --from=native /usr/lib/ /usr/lib/
COPY --from=native /usr/lib/jvm/java-11-openjdk /usr/lib/jvm/java-11-openjdk
COPY --from=native /etc/ssl/certs/java/cacerts /etc/ssl/certs/java/cacerts
RUN SCONE_MODE=sim scone binaryfs / /binary-fs.c \
    --include '/app/*' \
    --include '/lib/libz.so.1' \
    --include '/lib/libc.musl-x86_64.so.1' \
    --include '/lib/ld-musl-x86_64.so.1' \
    --include '/lib/libcrypto.so.1.1' \
    --include '/lib/libssl.so.1.1' \
    --include '/lib/libz.so.1.2.11' \
    --include '/usr/lib/libtls-standalone.so.1.0.0' \
    --include '/usr/lib/libtls-standalone.so.1' \
    --include '/usr/lib/libcrypto.so.1.1' \
    --include '/usr/lib/libssl.so.1.1' \
    --include '/usr/lib/libgif.so.7' \
    --include '/usr/lib/libxcb-xf86dri.so.0.0.0' \
    --include '/usr/lib/libxcb-dri2.so.0.0.0' \
    --include '/usr/lib/libxcb-composite.so.0' \
    --include '/usr/lib/libxcb-xvmc.so.0' \
    --include '/usr/lib/libturbojpeg.so.0' \
    --include '/usr/lib/libXdmcp.so.6.0.0' \
    --include '/usr/lib/libxcb-xf86dri.so.0' \
    --include '/usr/lib/libxcb-dpms.so.0.0.0' \
    --include '/usr/lib/libtasn1.so.6.5.6' \
    --include '/usr/lib/libxcb-xtest.so.0.0.0' \
    --include '/usr/lib/libxcb-damage.so.0' \
    --include '/usr/lib/libp11-kit.so.0.3.0' \
    --include '/usr/lib/libxcb-dri3.so.0.0.0' \
    --include '/usr/lib/libX11-xcb.so.1' \
    --include '/usr/lib/libxcb-xv.so.0' \
    --include '/usr/lib/libX11-xcb.so.1.0.0' \
    --include '/usr/lib/libxcb-xkb.so.1' \
    --include '/usr/lib/libxcb-shm.so.0' \
    --include '/usr/lib/libbz2.so.1.0.6' \
    --include '/usr/lib/libtasn1.so.6' \
    --include '/usr/lib/libxcb-dpms.so.0' \
    --include '/usr/lib/libxcb-res.so.0.0.0' \
    --include '/usr/lib/liblcms2.so.2.0.8' \
    --include '/usr/lib/libxcb-xfixes.so.0' \
    --include '/usr/lib/libxcb-render.so.0' \
    --include '/usr/lib/libffi.so.6.0.4' \
    --include '/usr/lib/libxcb-xtest.so.0' \
    --include '/usr/lib/libgif.so.7.1.0' \
    --include '/usr/lib/libbsd.so.0' \
    --include '/usr/lib/libxcb-xinput.so.0' \
    --include '/usr/lib/libxcb-record.so.0' \
    --include '/usr/lib/libXext.so.6.4.0' \
    --include '/usr/lib/libxcb-randr.so.0.1.0' \
    --include '/usr/lib/libxcb-xfixes.so.0.0.0' \
    --include '/usr/lib/libxcb-present.so.0' \
    --include '/usr/lib/libfreetype.so.6.17.0' \
    --include '/usr/lib/libxcb-xkb.so.1.0.0' \
    --include '/usr/lib/libasound.so.2' \
    --include '/usr/lib/libxcb-screensaver.so.0.0.0' \
    --include '/usr/lib/libxcb-randr.so.0' \
    --include '/usr/lib/libxcb-xinput.so.0.1.0' \
    --include '/usr/lib/libxcb-damage.so.0.0.0' \
    --include '/usr/lib/libxcb-composite.so.0.0.0' \
    --include '/usr/lib/libXrender.so.1' \
    --include '/usr/lib/libXext.so.6' \
    --include '/usr/lib/libjpeg.so.8.2.2' \
    --include '/usr/lib/libxcb-sync.so.1.0.0' \
    --include '/usr/lib/libxcb.so.1.1.0' \
    --include '/usr/lib/libjpeg.so.8' \
    --include '/usr/lib/libxcb-xvmc.so.0.0.0' \
    --include '/usr/lib/libturbojpeg.so.0.2.0' \
    --include '/usr/lib/libpng16.so.16.37.0' \
    --include '/usr/lib/libX11.so.6' \
    --include '/usr/lib/libbz2.so.1' \
    --include '/usr/lib/libXtst.so.6.1.0' \
    --include '/usr/lib/libX11.so.6.3.0' \
    --include '/usr/lib/libfreetype.so.6' \
    --include '/usr/lib/libxcb-record.so.0.0.0' \
    --include '/usr/lib/libXi.so.6' \
    --include '/usr/lib/libXdmcp.so.6' \
    --include '/usr/lib/libxcb-render.so.0.0.0' \
    --include '/usr/lib/libxcb-dri3.so.0' \
    --include '/usr/lib/libXau.so.6.0.0' \
    --include '/usr/lib/libxcb-screensaver.so.0' \
    --include '/usr/lib/libp11-kit.so.0' \
    --include '/usr/lib/libxcb-glx.so.0' \
    --include '/usr/lib/libxcb-xinerama.so.0.0.0' \
    --include '/usr/lib/libxcb-xinerama.so.0' \
    --include '/usr/lib/libxcb-xv.so.0.0.0' \
    --include '/usr/lib/libxcb-glx.so.0.0.0' \
    --include '/usr/lib/libXi.so.6.1.0' \
    --include '/usr/lib/libxcb-sync.so.1' \
    --include '/usr/lib/libxcb-present.so.0.0.0' \
    --include '/usr/lib/libffi.so.6' \
    --include '/usr/lib/libpng16.so.16' \
    --include '/usr/lib/libxcb-shape.so.0' \
    --include '/usr/lib/libxcb-shm.so.0.0.0' \
    --include '/usr/lib/libxcb-res.so.0' \
    --include '/usr/lib/libasound.so.2.0.0' \
    --include '/usr/lib/libXtst.so.6' \
    --include '/usr/lib/liblcms2.so.2' \
    --include '/usr/lib/libbsd.so.0.9.1' \
    --include '/usr/lib/libXau.so.6' \
    --include '/usr/lib/libxcb.so.1' \
    --include '/usr/lib/libxcb-shape.so.0.0.0' \
    --include '/usr/lib/libxcb-dri2.so.0' \
    --include '/usr/lib/libXrender.so.1.3.0' \
    --include '/usr/lib/jvm/java-11-openjdk/*' \
    --include '/etc/ssl/certs/java/cacerts/*'
RUN scone gcc /binary-fs.c -O0 -shared -o /libbinary-fs.so

FROM alpine:3.10
RUN apk add libgcc
RUN apk add libstdc++
COPY --from=native /lib/ld-musl-x86_64.so.1 /lib/ld-musl-x86_64.so.1
COPY --from=native /lib/libz.so.1 /lib/libz.so.1
COPY --from=native /usr/lib/jvm/java-11-openjdk/bin/../lib/jli/libjli.so /usr/lib/jvm/java-11-openjdk/bin/../lib/jli/libjli.so
COPY --from=cli /usr/lib/jvm/java-11-openjdk/bin/java /usr/lib/jvm/java-11-openjdk/bin/java
COPY --from=cli /opt/scone/lib /opt/scone/lib
COPY --from=cli /opt/scone/etc /opt/scone/etc
COPY --from=crosscompiler /libbinary-fs.so /libbinary-fs.so
RUN apk add --no-cache patchelf &&     patchelf --add-needed libbinary-fs.so /usr/lib/jvm/java-11-openjdk/bin/java &&     apk del patchelf
ENV LD_LIBRARY_PATH="/:/lib:/usr/lib/jvm/java-11-openjdk/bin/../lib/jli"
COPY --from=crosscompiler /usr/local/bin/scone-signer /usr/local/bin/scone-signer
RUN /usr/local/bin/scone-signer sign  --heap=4G --stack=1M --dlopen=2 /usr/lib/jvm/java-11-openjdk/bin/java
RUN rm /usr/local/bin/scone-signer
RUN ln -s /usr/lib/jvm/java-11-openjdk/bin/java /usr/bin/java
RUN MRENCLAVE="$(SCONE_HASH=1 /usr/lib/jvm/java-11-openjdk/bin/java)" && echo '{"mrenclave": "'$MRENCLAVE'"}'
ENTRYPOINT []
CMD ["java", "-jar", "/app/app.jar"]