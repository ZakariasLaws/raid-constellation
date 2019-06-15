#!/bin/bash

echo "Hello from $HOSTNAME acting as Predictor"
echo ""

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

check_env_dir EDGEINFERENCE_DIR
BIN_DIR=${EDGEINFERENCE_DIR}/bin

CONSTELLATION_PORT=$1; shift
serverAddress=$1; shift
nrNodes=$1; shift
classname=$1; shift
poolName=$1; shift
clientTimeout=$1; shift
context=$1; shift
args="-role PREDICTOR -context ${context} $@"

tmpdir=${EDGEINFERENCE_DIR}/.java_io_tmpdir
mkdir -p ${tmpdir}

java -cp ${EDGEINFERENCE_DIR}/lib/*:${CLASSPATH} \
        -Djava.rmi.server.hostname=localhost \
        -Djava.io.tmpdir=${tmpdir} \
        -Dlog4j.configuration=file:${EDGEINFERENCE_DIR}/log4j.properties \
        -Dibis.server.address=${serverAddress}:${CONSTELLATION_PORT} \
        -Dibis.pool.size=${nrNodes} \
        -Dibis.server.port=${CONSTELLATION_PORT} \
        -Dibis.pool.name=${poolName} \
        -Dibis.constellation.closed=true \
        ${classname} \
        ${args}


if [[ ${clientTimeout} -lt 0 ]]; then
  while :; do
    sleep 10
  done
else
  echo "Shutting down connection in $clientTimeout seconds"
  sleep ${clientTimeout}
fi
