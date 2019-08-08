#!/bin/bash

address=$1; shift
role=$1; shift
serverIp=$1; shift
poolName=$1; shift
params="$@"

echo "*********** STARTING ${role} remotely on ${address} ***********"

echo "address: ${address}"
echo "role: ${role}"
echo "server ip: ${serverIp}"
echo "pool: ${poolName}"
echo "params: ${params}"

hostName=`echo ${address} | awk -F'@' '{print $1}'`

# Check if we are on local machine
if [[ ${hostName} == `echo ${USER}` ]]; then
    cd ${EDGEINFERENCE_DIR} && ${EDGEINFERENCE_DIR}/bin/distributed/run.bash ${role} ${serverIp} ${poolName} ${params}
    exit
fi

ssh -t ${address} "\${EDGEINFERENCE_DIR}/bin/distributed/run.bash ${role} ${serverIp} ${poolName}} ${params}
${params}"
