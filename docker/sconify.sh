#!/bin/bash
# ./sconify.sh docker.io/iexechub/tee-worker-pre-compute:dev docker.io/iexechub/tee-worker-pre-compute:dev-debug
cd $(dirname $0)

IMG_FROM=$1
IMG_TO=$2

ARGS=$(sed -e "s'\${IMG_FROM}'${IMG_FROM}'" -e "s'\${IMG_TO}'${IMG_TO}'" sconify.args)
echo $ARGS

/bin/bash -c "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
            registry.scontain.com:5050/sconecuratedimages/iexec-sconify-image:5.3.3 \
            sconify_iexec $ARGS"