#!/usr/bin/env bash

# Executes EdgeInference with Constellation using configurations from
# bin/distributed/config
function check_env() {
    local name_env_dir=$1
    if [[ -z ${!name_env_dir} ]]
    then
	echo "Environment variable $name_env_dir has not been set"
	exit 1
    fi
}

function check_env_dir() {
    local name_env_dir=$1

    check_env ${name_env_dir}

    if [[ ! -d ${!name_env_dir} ]]
    then
	echo "Environment variable $name_env_dir does not represent a directory"
	exit 1
    fi
}

function usage() {
    echo "Usage:"
    echo "./bin/distributed/run.bash <role[s/p/t]> <server_address> <poolname> <possible_contexts>"
    echo ""
    echo "For example, in order to start a predictor with contexts A, B and C:"
    echo "./bin/distributed/run.bash p 10.72.34.117 my.pool.name A,B,C"
    echo "To start a source targeting activity 0:1:0 with results:"
    echo "./bin/distributed/run.bash s 10.72.34.117 my.pool.name A,B,C -target 0:1:0"
    echo ""
    echo "Remember to start Constellation server first"
}

check_env_dir EDGEINFERENCE_DIR
BIN_DIR=${EDGEINFERENCE_DIR}/bin

check_env CONSTELLATION_PORT

tmpdir=${EDGEINFERENCE_DIR}/.java_io_tmpdir
mkdir -p ${tmpdir}

role=$1; shift
serverAddress=$1; shift
poolName=$1; shift
context=$1; shift

if [[ -z ${role} || -z ${serverAddress} || -z ${poolName} ]]; then
    usage
    exit 1
fi

roleFull=""
if [[ ${role,,} == "p" ]]; then
    if [[ -z ${context} ]]; then
        usage
        exit 1
    fi

    args="-role PREDICTOR -context ${context} $@"
    roleFull="Predictor"
elif [[ ${role,,} == "s" ]]; then
    if [[ -z ${context} ]]; then
        usage
        exit 1
    fi

    args="-role SOURCE -context ${context} $@"
    roleFull="Source"
else
    args="-role TARGET $@"
    roleFull="Target"
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
    ${pre}.queue.limit=1000 "
elif [[ ${role,,} == "p" ]]; then
    command="\
    ${pre}.remotesteal.throttle=true \
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
        -Dibis.constellation.closed=false \
        -Dibis.constelaltion.distributed=true \
        ${command} \
        ${classname} \
        ${args}
