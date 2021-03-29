#!/bin/bash
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}
# check docker daemon
set +e
LOG_INFO "Stopping Fabric demo network..."
if docker ps >/dev/null; then
    docker stop cli
    docker stop peer0.org2.example.com
    docker stop peer0.org1.example.com
    docker stop peer1.org2.example.com
    docker stop peer1.org1.example.com
    docker stop orderer.example.com
    docker ps
fi
set -e
LOG_INFO "Fabric demo network has been stopped."
