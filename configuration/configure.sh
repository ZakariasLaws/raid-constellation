#!/bin/bash

#DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

echo "NOTE: Run configuration script from root of git as such: ./configuration/config.sh"

function isNumber {
  re='^[0-9]+$'
  if ! [[ $1 =~ $re ]] ; then
    echo "$1 is not a valid port number" >&2
    exit 1
  fi
}

echo "This script will set the necessary configurations, the config file will be placed in the provided bin dir."

read -rp "Constellation port: " constPort

isNumber $constPort

read -rep "Edgeinference build location (dir where bin is located): " binDir

if ! [[ -d $binDir ]]; then
  echo "$binDir is not a directory"
  exit 1
fi  

read -rep "TensorFlow serving config: " tfConf

if ! [[ -f $tfConf ]]; then
  echo "$tfConf does not exist"
  exit 1
fi

read -rp "TensorFlow serving port: " tfPort

isNumber $tfPort

CONFIG_FILE="$binDir/config.RAID"

echo "CONSTELLATION_PORT=$constPort" > $CONFIG_FILE
echo "EDGEINFERENCE_DIR=$binDir" >> $CONFIG_FILE
echo "EDGEINFERENCE_SERVING_PORT=$tfPort" >> $CONFIG_FILE
echo "EDGEINFERENCE_SERVING_CONFIG=$tfConf" >> $CONFIG_FILE

echo "Created config file succesfully"
