#!/bin/bash

# READ CONFIG FILE
CONF_FILE="${RAID_DIR}/config.RAID"
CONSTELLATION_PORT="$( cut -d'=' -f2 <<< "$(sed -n '1p' $CONF_FILE)")"
TENSORFLOW_SERVING="$( cut -d'=' -f2 <<< "$(sed -n '2p' $CONF_FILE)")"
TENSORFLOW_SERVING_CONFIG="$( cut -d'=' -f2 <<< "$(sed -n '3p' $CONF_FILE)")"

if [[ -z ${CONSTELLATION_PORT} ]] || [[ -z ${TENSORFLOW_SERVING_CONFIG} ]]; then
  echo "Config file either missing or corrupted"
  exit 1
fi

if [[ ${TENSORFLOW_SERVING} != docker ]] && [[ -z ${TENSORFLOW_SERVING} ]]; then
  echo "Config file either missing or corrupted"
  exit 1
fi

TENSORFLOW_MODELS=$(dirname $TENSORFLOW_SERVING_CONFIG)/models/

docker run \
-p $((${TENSORFLOW_SERVING_PORT} - 1)):8500 \
-p ${TENSORFLOW_SERVING_PORT}:8501 \
--mount type=bind,source=$TENSORFLOW_MODELS,target=$TENSORFLOW_MODELS \
--mount type=bind,source=$TENSORFLOW_SERVING_CONFIG,target=$TENSORFLOW_SERVING_CONFIG \
-t tensorflow/serving --model_config_file=$TENSORFLOW_SERVING_CONFIG
