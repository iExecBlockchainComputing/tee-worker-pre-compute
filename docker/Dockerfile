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
### Build the final image with
### the produced jar.
###

FROM registry.scontain.com:5050/sconecuratedimages/apps:jdk-alpine-scone3.0

COPY --from=gradle-builder $JAR_PATH /app/tee-worker-pre-compute.jar

RUN SCONE_MODE=sim SCONE_HEAP=128M                                          \
    && mkdir conf                                                           \
    && scone fspf create fspf.pb                                            \
    && scone fspf addr fspf.pb /  --not-protected --kernel /                \
    && scone fspf addr fspf.pb /usr --authenticated --kernel /usr           \
    && scone fspf addf fspf.pb /usr /usr                                    \
    && scone fspf addr fspf.pb /bin --authenticated --kernel /bin           \
    && scone fspf addf fspf.pb /bin /bin                                    \
    && scone fspf addr fspf.pb /lib --authenticated --kernel /lib           \
    && scone fspf addf fspf.pb /lib /lib                                    \
    && scone fspf addr fspf.pb /etc/ssl --authenticated --kernel /etc/ssl   \
    && scone fspf addf fspf.pb /etc/ssl /etc/ssl                            \
    && scone fspf addr fspf.pb /sbin --authenticated --kernel /sbin         \
    && scone fspf addf fspf.pb /sbin /sbin                                  \
    && scone fspf addr fspf.pb /app --authenticated --kernel /app           \
    && scone fspf addf fspf.pb /app /app                                    \
    && scone fspf encrypt ./fspf.pb > /conf/keytag                          \
        && MRENCLAVE="$(SCONE_HASH=1 SCONE_HEAP=3G java)"                   \
    && FSPF_TAG=$(cat conf/keytag | awk '{print $9}')                       \
    && FSPF_KEY=$(cat conf/keytag | awk '{print $11}')                      \
    && FINGERPRINT="$FSPF_KEY|$FSPF_TAG|$MRENCLAVE"                         \
    && echo $FINGERPRINT > conf/fingerprint.txt                             \
    && printf "\n########################################################\nMREnclave: \n$FINGERPRINT\n########################################################\n\n"

# /!\ this should match the "command" entry in the SMS's palaemon config
ENTRYPOINT [ "/bin/sh", "-c", "java -jar /app/tee-worker-pre-compute.jar" ]
