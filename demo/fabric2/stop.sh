#!/bin/bash
SHELL_FOLDER="$(cd "$(dirname "$0")" && pwd)"
FABRIC_NETWORK_DIR=${SHELL_FOLDER}/fabric-samples-2.3.0/test-network/

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}
# check docker daemon

check_fabric_network() {
    if [ ! -e ${FABRIC_NETWORK_DIR}/network.sh ]; then
        LOG_INFO "Fabric2 demo has not been built. Ignored."
        exit 0
    fi
}

check_fabric_network


set +e
LOG_INFO "Stopping Fabric2 demo network..."
if docker ps >/dev/null; then
    docker stop cli
    docker stop peer0.org2.example.com
    docker stop peer0.org1.example.com
    docker stop orderer.example.com
    docker ps
fi
set -e
LOG_INFO "Fabric2 demo network has been stopped."
