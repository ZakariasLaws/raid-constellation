#!/bin/bash

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

check_env CONSTELLATION_PORT
port=${CONSTELLATION_PORT}

JAVA_IO_TEMP_DIR=$EDGEINFERENCE_DIR/.java_io_tmp
mkdir -p ${JAVA_IO_TEMP_DIR}

classname=nl.zakarias.constellation.edgeinference.EdgeInference

# Generate class paths
source ${BIN_DIR}/create_class_path.bash ${EDGEINFERENCE_DIR}
classpath=$(createClassPath ${EDGEINFERENCE_DIR} lib/edgeinference-constellation.jar)

java -cp ${classpath}:${CLASSPATH} -Xmx2G \
    -Dibis.server.address=localhost:${port} \
    -Dibis.constellation.distributed=false \
    ${classname}
    $*

