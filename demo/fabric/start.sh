#!/bin/bash

SHELL_FOLDER="$(cd "$(dirname "$0")" && pwd)"
FABRIC_NETWORK_DIR=${SHELL_FOLDER}/fabric-samples-1.4.4/first-network/

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}
# check docker daemon
check_docker_service() {
    set +e
    if ! docker ps >/dev/null; then
        LOG_INFO "Please install docker and add your user by:"
        echo -e "\033[32m        sudo gpasswd -a ${USER} docker && su ${USER}\033[0m"
        exit 1
    fi
    set -e
}

check_fabric_network() {
    if [ ! -e ${FABRIC_NETWORK_DIR}/byfn.sh ]; then
        LOG_INFO "Fabric1 demo has not been built. Ignored."
        exit 0
    fi
}

check_fabric_network
check_docker_service
LOG_INFO "Starting Fabric1 demo network..."
docker start cli
docker start peer0.org2.example.com
docker start peer0.org1.example.com
docker start peer1.org2.example.com
docker start peer1.org1.example.com
docker start orderer.example.com
docker ps
LOG_INFO "Fabric1 demo network has been started."
