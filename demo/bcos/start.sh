#!/bin/bash

SHELL_FOLDER="$(cd "$(dirname "$0")" && pwd)"

LOG_ERROR() {
    echo -e "\033[31m[ERROR] $@\033[0m"
}

LOG_INFO() {
    echo -e "\033[32m[INFO] $@\033[0m"
}

if [ ! -e ${SHELL_FOLDER}/nodes/127.0.0.1/start_all.sh ] && [ ! -e ${SHELL_FOLDER}/nodes_gm/127.0.0.1/start_all.sh ]; then
    LOG_ERROR "FISCO-BCOS demo has not been built."
    LOG_INFO "Please use \"build.sh\" directly."
    exit 1
fi

if [ -e ${SHELL_FOLDER}/nodes/127.0.0.1/start_all.sh ]; then
    bash ${SHELL_FOLDER}/nodes/127.0.0.1/start_all.sh
fi

if [ -e ${SHELL_FOLDER}/nodes_gm/127.0.0.1/start_all.sh ]; then
    bash ${SHELL_FOLDER}/nodes_gm/127.0.0.1/start_all.sh
fi

