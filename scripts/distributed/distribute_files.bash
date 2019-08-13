#!/bin/bash

# Executes RaidConstellation with Constellation using configurations from config.bash
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

check_env_dir RAID_DIR
BIN_DIR=${RAID_DIR}/bin
ROOT=${RAID_DIR}/../../../
LOC='/Constellation/raid-constellation'

source ${BIN_DIR}/distributed/config

for ip in "${computeAddresses[@]}"
do
  arrIp=(${ip})
  echo "------ ${arrIp[0]} ------"
  scp -r ${ROOT}/src/main ${arrIp[0]}:"~$LOC/src"
  scp -r ${ROOT}/scripts/* ${arrIp[0]}:"~$LOC/scripts"
  scp -r ${ROOT}/Constellation/lib/* ${arrIp[0]}:"~$LOC/Constellation/lib"
  echo ""
done

arrSource=(${sourceAddress})
if [[ "${arrSource[0]}" != "self" ]]; then
    echo "------ ${arrSource[0]} ------"
    scp -r ${ROOT}/src/main ${arrSource[0]}:"~$LOC/src"
    scp -r ${ROOT}/scripts/* ${arrSource[0]}:"~$LOC/scripts"
    scp -r ${ROOT}/Constellation/lib/* ${arrSource[0]}:"~$LOC/Constellation/lib"
    echo ""
fi

if [[ "${targetAddress}" != "self" ]]; then
    echo "------ ${targetAddress} ------"
    scp -r ${ROOT}/src/main ${targetAddress}:"~$LOC/src"
    scp -r ${ROOT}/scripts/* ${targetAddress}:"~$LOC/scripts"
    scp -r ${ROOT}/Constellation/lib/* ${targetAddress}:"~$LOC/Constellation/lib"
    echo ""
fi