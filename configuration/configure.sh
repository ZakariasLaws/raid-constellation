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

#DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

echo "NOTE: Run configuration script from root of git as such: ./configuration/config.sh"

check_env_dir RAID_DIR

function isNumber {
  re='^[0-9]+$'
  if ! [[ $1 =~ $re ]] ; then
    echo "$1 is not a valid port number" >&2
    exit 1
  fi
}

echo "This script will set the necessary configurations, the config file will be placed in the provided bin dir."

read -rp "Constellation port: " constPort

isNumber ${constPort}

read -rep "Tensorflow Serving Binary location (if using docker, type 'docker'): " tfBin

if [[ $tfBin == docker ]]; then
    echo "No binary, using docker"
elif ! [[ -f $tfBin ]]; then
  echo "${tfBin} does not exist"
  exit 1
fi

read -rep "TensorFlow serving config: " tfConf

if ! [[ -f ${tfConf} ]]; then
  echo "${tfConf} does not exist"
  exit 1
fi

CONFIG_FILE="${RAID_DIR}/config.RAID"

echo "CONSTELLATION_PORT=$constPort" > $CONFIG_FILE
echo "TENSORFLOW_BIN=$tfBin" >> $CONFIG_FILE
echo "TENSORFLOW_SERVING_CONFIG=${tfConf}" >> $CONFIG_FILE


if [[ ! -f $CONFIG_FILE ]]; then
    echo "Something went wrong, check environment variables and try again"
    exit 1
fi

if [[ ! -d "${RAID_DIR}/../../../tensorflow/tensorflow_serving/models" ]]; then
  echo "--------------------"
  echo "No models directory found \"tensorflow/tensorflow_serving/models\""
  echo "Either manually add these or extract tensorflow/tensorflow_serving/models.tgz"
  echo "Remember to modify tensorflow/tensorflow_serving/ModelServerConfig.conf"
  echo "--------------------"
fi

echo "Created config file successfully"
