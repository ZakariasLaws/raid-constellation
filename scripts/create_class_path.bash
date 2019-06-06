#!/bin/bash

createClassPath() {
    basedir=$1
    jar=$2
    classpath="$1/$2"

    if [[ -d ${basedir}/lib ]]
    then
	for i in ${basedir}/lib/*.jar
	do
	    classpath+=":$i"
	done
    fi
    echo ${classpath}
}
