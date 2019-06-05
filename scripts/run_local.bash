#!/bin/bash

function check_env_dir() {
    local name_env_dir=$1
    if [ -z ${!name_env_dir} ]
    then
	echo "Environment variable $name_env_dir has not been set"
	exit 1
    fi

    if [ ! -d ${!name_env_dir} ]
    then
	echo "Environment variable $name_env_dir does not represent a directory"
	exit 1
    fi
}

echo "test"

