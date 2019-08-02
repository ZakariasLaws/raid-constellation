#!/usr/bin/env bash

# Executes EdgeInference with Constellation

# Kill all child processes
trap 'trap - SIGTERM && kill -- -$$' SIGINT SIGTERM EXIT

function usage() {
    echo "Usage:"
    echo "./bin/distributed/run.bash <role[s/p/t]> <server_address> <poolname> <possible_contexts>"
    echo ""
    echo "For example, in order to start a predictor with contexts A, B and C:"
    echo "./bin/distributed/run.bash p 10.72.34.117 my.pool.name A,B,C"
    echo ""
    echo "To start a source targeting activity 0:1:0 with results, sending batches of 10 images per time:"
    echo "./bin/distributed/run.bash s 10.72.34.117 my.pool.name A,B,C -target 0:1:0 -batchSize 10"
    echo ""
    echo "Remember to start Constellation server first"
}

# READ CONFIG FILE

CONF_FILE="./config.RAID"
CONSTELLATION_PORT="$( cut -d'=' -f2 <<< "$(sed -n '1p' $CONF_FILE)")"
EDGEINFERENCE_DIR="$( cut -d'=' -f2 <<< "$(sed -n '2p' $CONF_FILE)")"
EDGEINFERENCE_SERVING_PORT="$( cut -d'=' -f2 <<< "$(sed -n '3p' $CONF_FILE)")"
EDGEINFERENCE_SERVING_CONFIG="$( cut -d'=' -f2 <<< "$(sed -n '4p' $CONF_FILE)")"

if [[ ! "${EDGEINFERENCE_DIR: -1}" == "/" ]]; then
  EDGEINFERENCE_DIR="${EDGEINFERENCE_DIR}/"
fi

if [[ -z ${CONSTELLATION_PORT} ]] || [[ -z ${EDGEINFERENCE_DIR} ]] || [[ -z ${EDGEINFERENCE_SERVING_PORT} ]] || [[ -z ${EDGEINFERENCE_SERVING_CONFIG} ]]; then
  echo "Config file either missing or corrupted"
  exit 1
fi

tmpdir=${EDGEINFERENCE_DIR}/.java_io_tmpdir
mkdir -p ${tmpdir}

role=$1; shift
serverAddress=$1; shift
poolName=$1; shift

if [[ -z ${role} || -z ${serverAddress} || -z ${poolName} ]]; then
    usage
    exit 1
fi

if [[ ${role,,} == "p" ]]; then
    context=$1; shift
    if [[ -z ${context} ]]; then
        usage
        exit 1
    fi

    args="-role PREDICTOR -context ${context} $*"
elif [[ ${role,,} == "s" ]]; then
    context=$1; shift
    if [[ -z ${context} ]]; then
        usage
        exit 1
    fi

    args="-role SOURCE -context ${context} $*"
else
    args="-role TARGET $*"
fi

classname="nl.zakarias.constellation.edgeinference.EdgeInference"

echo "**** Starting with following config ****"
echo "Poolname: ${poolName}"
echo "Server address: ${serverAddress}:${CONSTELLATION_PORT}"
echo "Context: ${context}"

# Add system properties specific for each instance
command=""
pre="-Dibis.constellation"
if [[ ${role,,} == "s" ]]; then
    command="\
    ${pre}.queue.limit=1 "
elif [[ ${role,,} == "p" ]]; then
    tfServer=`which tensorflow_model_server`
    if [[ ${tfServer} == "" ]]; then
        echo "Add tensorflow_model_server to PATH"
        exit 1
    fi

    if [[ $(ps -C tensorflow_model_server | grep tensorflow_mode) ]]; then
        echo ""
        echo "****************"
        echo "Using existing TensorFlow Model Serving instance, log can be found at: ${EDGEINFERENCE_DIR}/tensorflow_model_server.log"
        echo "****************"
        echo ""
    else
        # Start model serving in background, stores log in tensorflow_model_server.log
        echo ""
        echo "****************"
        echo "Starting TensorFlow Model Serving, log can be found at: ${EDGEINFERENCE_DIR}/tensorflow_model_server.log"
        nohup tensorflow_model_server --port=$((${EDGEINFERENCE_SERVING_PORT} - 1)) --rest_api_port=${EDGEINFERENCE_SERVING_PORT} --model_config_file=${EDGEINFERENCE_SERVING_CONFIG} > ${EDGEINFERENCE_DIR}/tensorflow_model_server.log &
        echo "****************"
        echo ""

        sleep 3
    fi

    # Predictor will steal activities and should be allowed to leave the pool
    command="\
    ${pre}.remotesteal.throttle=false \
    ${pre}.remotesteal.size=1 \
    ${pre}.allowLeave=true \
    "
elif [[ ${role,,} == "t" ]]; then
    # Target will never steal activities, but only process events
    command="\
    ${pre}.remotesteal.throttle=false \
    ${pre}.remotesteal.size=1 \
    "
else
    command="\
    "
fi

java -cp ${EDGEINFERENCE_DIR}/lib/*:${CLASSPATH} \
        -Djava.rmi.server.hostname=localhost \
        -Djava.io.tmpdir=${tmpdir} \
        -Dlog4j.configuration=file:${EDGEINFERENCE_DIR}/log4j.properties \
        -Dibis.server.address=${serverAddress}:${CONSTELLATION_PORT} \
        -Dibis.server.port=${CONSTELLATION_PORT} \
        -Dibis.pool.name=${poolName} \
        -Dibis.constellation.profile=true \
        -Dibis.constellation.profile.output=gantt \
        -Dibis.constellation.closed=false \
        -Dibis.constellation.distributed=true \
        -Dibis.constellation.ignoreEmptyReplies=true \
        -Dibis.io.serialization.object.default=sun \
        ${command} \
        ${classname} \
        ${args}
