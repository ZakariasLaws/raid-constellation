#!/usr/bin/env bash

# Executes RAID with Constellation

# Kill all child processes
trap 'trap - SIGTERM && kill -- -$$' SIGINT SIGTERM EXIT

function usage() {
    echo "Usage:"
    echo "./bin/distributed/run.bash <role[s/p/t]> <server_address> <poolname> <possible_contexts> <others>"
    echo ""
    echo "For example, in order to start a predictor with contexts A, B and C:"
    echo "./bin/distributed/run.bash p 10.72.34.117 my.pool.name -context A,B,C"
    echo ""
    echo "To start a source targeting activity 0:1:0 with results, sending batches of 10 images per time:"
    echo "./bin/distributed/run.bash s 10.72.34.117 my.pool.name -context A,B,C -target 0:1:0 -batchSize 10 -dataDir /home/username/MNIST_DATA -modelName mnist"
    echo ""
    echo "Remember to start Constellation server first"
}

# READ CONFIG FILE

CONF_FILE="${RAID_DIR}/config.RAID"
CONSTELLATION_PORT="$( cut -d'=' -f2 <<< "$(sed -n '1p' $CONF_FILE)")"
RAID_DIR="$( cut -d'=' -f2 <<< "$(sed -n '2p' $CONF_FILE)")"
TENSORFLOW_SERVING="$( cut -d'=' -f2 <<< "$(sed -n '3p' $CONF_FILE)")"
TENSORFLOW_SERVING_PORT="$( cut -d'=' -f2 <<< "$(sed -n '4p' $CONF_FILE)")"
TENSORFLOW_SERVING_CONFIG="$( cut -d'=' -f2 <<< "$(sed -n '5p' $CONF_FILE)")"

if [[ ! "${RAID_DIR: -1}" == "/" ]]; then
  RAID_DIR="${RAID_DIR}/"
fi

if [[ -z ${CONSTELLATION_PORT} ]] || [[ -z ${RAID_DIR} ]] || [[ -z ${TENSORFLOW_SERVING_PORT} ]] || [[ -z ${TENSORFLOW_SERVING_CONFIG} ]]; then
  echo "Config file either missing or corrupted"
  exit 1
fi

tmpdir=${RAID_DIR}/.java_io_tmpdir
mkdir -p ${tmpdir}

role=$1; shift
serverAddress=$1; shift
poolName=$1; shift
params="$@"

if [[ -z ${role} || -z ${serverAddress} || -z ${poolName} ]]; then
    usage
    exit 1
fi

# Test if profile output file was provides, HAS TO BE LAST ARGUMENT
# -profileOutput <path>
profileOutput="gantt"

str=${params}
delimiter="-profileOutput "
s=${str}${delimiter}
array=();
while [[ $s ]]; do
    array+=( "${s%%"${delimiter}"*}" );
    s=${s#*"${delimiter}"};
done;

if [[ ${#array[1]} -gt 0 ]]; then
    IFS=' '
    read -ra ADDR <<< "${array[1]}"
    profileOutput=${ADDR[0]}
fi

params=${array[0]}

if [[ ${role,,} == "p" ]]; then
    args="-role PREDICTOR ${params}"
elif [[ ${role,,} == "s" ]]; then
    args="-role SOURCE ${params}"
else
    args="-role TARGET ${params}"
fi

classname="nl.zakarias.constellation.raid.RaidConstellation"

echo "**** Starting with following config ****"
echo "Poolname: ${poolName}"
echo "Server address: ${serverAddress}:${CONSTELLATION_PORT}"
echo "Params: ${params}"

# Add system properties specific for each instance
command=""
pre="-Dibis.constellation"
if [[ ${role,,} == "s" ]]; then
    command="\
    ${pre}.queue.limit=1 "
elif [[ ${role,,} == "p" ]]; then
    if [[ ! -f ${TENSORFLOW_SERVING} ]]; then
        echo "Could not read TensorFlow serving binary, check that the config file has the correct path"
        exit 1
    fi

    if [[ $(ps -C tensorflow_model_server | grep tensorflow_mode) ]]; then
        echo ""
        echo "****************"
        echo "Using existing TensorFlow Model Serving instance, log can be found at: ${RAID_DIR}/tensorflow_model_server.log"
        echo "****************"
        echo ""
    else
        # Start model serving in background, stores log in tensorflow_model_server.log
        echo ""
        echo "****************"
        echo "Starting TensorFlow Model Serving, log can be found at: ${RAID_DIR}/tensorflow_model_server.log"
        nohup ${TENSORFLOW_SERVING} --port=$((${TENSORFLOW_SERVING_PORT} - 1)) --rest_api_port=${TENSORFLOW_SERVING_PORT} --model_config_file=${TENSORFLOW_SERVING_CONFIG} > ${RAID_DIR}/tensorflow_model_server.log &
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

java -cp ${RAID_DIR}/lib/*:${CLASSPATH} \
        -Djava.rmi.server.hostname=localhost \
        -Djava.io.tmpdir=${tmpdir} \
        -Dlog4j.configuration=file:${RAID_DIR}/log4j.properties \
        -Dibis.server.address=${serverAddress}:${CONSTELLATION_PORT} \
        -Dibis.server.port=${CONSTELLATION_PORT} \
        -Dibis.pool.name=${poolName} \
        -Dibis.constellation.profile=true \
        -Dibis.constellation.profile.output=${profileOutput} \
        -Dibis.constellation.closed=false \
        -Dibis.constellation.distributed=true \
        -Dibis.constellation.ignoreEmptyReplies=true \
        -Dibis.io.serialization.object.default=sun \
        ${command} \
        ${classname} \
        ${args}
