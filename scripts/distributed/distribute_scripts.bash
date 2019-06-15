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

source ${BIN_DIR}/distributed/config

for ip in "${computeAddresses[@]}"
do
  arrIp=(${ip})
  echo "------ ${arrIp[0]} ------"
  scp -r ${BIN_DIR}/distributed ${arrIp[0]}:"\${EDGEINFERENCE_DIR}/bin"
  scp -r ${EDGEINFERENCE_DIR}/lib/edgeinference-constellation.jar ${arrIp[0]}:"\${EDGEINFERENCE_DIR}/lib/"
  echo ""
done

arrSource=(${sourceAddress})
if [[ "${arrSource[0]}" != "self" ]]; then
    echo "------ ${arrSource[0]} ------"
    scp -r ${BIN_DIR}/distributed ${arrSource[0]}:"\${EDGEINFERENCE_DIR}/bin"
    scp -r ${EDGEINFERENCE_DIR}/lib/edgeinference-constellation.jar ${arrSource[0]}:"\${EDGEINFERENCE_DIR}/lib/"
    echo ""
fi

if [[ "${targetAddress}" != "self" ]]; then
    echo "------ ${targetAddress} ------"
    scp -r ${BIN_DIR}/distributed ${targetAddress}:"\${EDGEINFERENCE_DIR}/bin"
    scp -r ${EDGEINFERENCE_DIR}/lib/edgeinference-constellation.jar ${targetAddress}:"\${EDGEINFERENCE_DIR}/lib/"
    echo ""
fi