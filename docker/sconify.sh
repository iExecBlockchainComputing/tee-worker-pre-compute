#!/bin/bash

docker run -it --rm \
            -v /var/run/docker.sock:/var/run/docker.sock \
            registry.scontain.com:5050/sconecuratedimages/iexec-sconify-image:5.3.3 \
            sconify_iexec \
                --name=tee-post-compute \
                --from=nexus.iex.ec/tee-worker-post-compute:dev \
                --to=nexus.iex.ec/tee-worker-post-compute:dev-tee \
                --binary-fs \
                --fs-dir=/app \
                --host-path=/etc/hosts \
                --host-path=/etc/resolv.conf \
                --binary="/usr/lib/jvm/java-11-openjdk/bin/java" \
                --heap="4G" \
                --dlopen="2" \
                --verbose \
                --no-color \
                --command="java -jar /app/app.jar"
