#!/bin/bash
# copy and download requirements for demo.tar.gz

set -e
LANG=en_US.UTF-8
ROOT=$(
    cd "$(dirname "$0")"
    pwd
)
WECROSS_ROOT=${ROOT}/../

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

version_file="profile_version.sh"
[[ ! -f "${version_file}" ]] && {
  LOG_ERROR " ${version_file} not exist, please check if the demo is the latest. "
  exit 1
}

source "${version_file}"
LOG_INFO "source ${version_file}, WeCross Version=${WECROSS_VERSION}"

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -LO ${url}
    fi
}

prepare_bcos() {
    cd ${ROOT}/bcos/
    # Download
    LOG_INFO "Download build_chain.sh ..."
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${BCOS_VERSION}/build_chain.sh
    chmod u+x build_chain.sh

    LOG_INFO "Download get_account.sh ..."
    Download https://raw.githubusercontent.com/FISCO-BCOS/console/master/tools/get_account.sh
    chmod u+x get_account.sh

    LOG_INFO "Download get_gm_account.sh ..."
    Download https://raw.githubusercontent.com/FISCO-BCOS/console/master/tools/get_gm_account.sh
    chmod u+x get_gm_account.sh

    LOG_INFO "Download fisco-bcos binary"
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${BCOS_VERSION}/fisco-bcos.tar.gz
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${BCOS_VERSION}/fisco-bcos-macOS.tar.gz

    cd -
}

prepare_fabric() {
    cd ${ROOT}/fabric/
    # Download
    LOG_INFO "Download fabric tools ..."
    Download https://github.com/hyperledger/fabric/releases/download/v1.4.6/hyperledger-fabric-darwin-amd64-1.4.6.tar.gz
    Download https://github.com/hyperledger/fabric/releases/download/v1.4.6/hyperledger-fabric-linux-amd64-1.4.6.tar.gz

    LOG_INFO "Download fabric samples ..."
    Download https://github.com/hyperledger/fabric-samples/archive/v1.4.4.tar.gz

    cd -
}

prepare_wecross() {
    cd ${ROOT}
    LOG_INFO "Copy WeCross scripts"
    cp ${WECROSS_ROOT}/scripts/download_wecross.sh ./
    cp ${WECROSS_ROOT}/scripts/download_console.sh ./
    cp ${WECROSS_ROOT}/scripts/download_account_manager.sh ./
}

main() {
    if [ -n "$1" ]; then
        WECROSS_ROOT=$1/
    fi

    prepare_bcos
    prepare_fabric
    prepare_wecross
}

main $@
