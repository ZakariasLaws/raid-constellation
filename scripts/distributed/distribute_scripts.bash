#!/bin/bash

# Executes EdgeInference with Constellation using configurations from config.bash
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

source ${BIN_DIR}/distributed/config.bash

for ip in "${computeAddresses[@]}"
do
  scp -r ${BIN_DIR} ${ip}:"\${EDGEINFERENCE_DIR}/bin"
done
