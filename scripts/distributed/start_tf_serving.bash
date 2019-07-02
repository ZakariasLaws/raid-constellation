#!/usr/bin/env bash

if ! [[ -x "$(command -v tensorflow_model_server)" ]]; then
    # Command exists in PATH
    nohup tensorflow_model_server --port=8500 --rest_api_port=8501 --model_config_file=${EDGEINFERENCE_DIR}/../../../tensorflow/tensorflow_serving/ModelServerConfig.conf > tensorflow_model_server.log &
else
    nohup ${1} --port=8500 --rest_api_port=8501 --model_base_path=${EDGEINFERENCE_DIR}/../../../tensorflow/tensorflow_serving/ModelServerConfig.conf > tensorflow_model_server.log &
fi
