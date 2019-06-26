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
ROOT=${EDGEINFERENCE_DIR}/../../../

source ${BIN_DIR}/distributed/config

for ip in "${computeAddresses[@]}"
do
  arrIp=(${ip})
  echo "------ ${arrIp[0]} ------"
  scp -r ${ROOT}/src/main ${arrIp[0]}:"\${EDGEINFERENCE_DIR}/../../../src"
  scp -r ${ROOT}/scripts/* ${arrIp[0]}:"\${EDGEINFERENCE_DIR}/../../../scripts"
  echo ""
done

arrSource=(${sourceAddress})
if [[ "${arrSource[0]}" != "self" ]]; then
    echo "------ ${arrSource[0]} ------"
    scp -r ${ROOT}/distributed ${arrSource[0]}:"\${EDGEINFERENCE_DIR}/../../.../src"
    scp -r ${ROOT}/scripts/* ${arrSource[0]}:"\${EDGEINFERENCE_DIR}/../../../scripts"
    echo ""
fi

if [[ "${targetAddress}" != "self" ]]; then
    echo "------ ${targetAddress} ------"
    scp -r ${ROOT}/distributed ${targetAddress}:"\${EDGEINFERENCE_DIR}/../../../src"
    scp -r ${ROOT}/scripts/* ${targetAddress}:"\${EDGEINFERENCE_DIR}/../../../scripts"
    echo ""
fi