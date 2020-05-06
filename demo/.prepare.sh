#!/bin/bash
# copy and download requirements for demo.tar.gz

set -e
LANG=en_US.utf8
ROOT=$(cd "$(dirname "$0")";pwd)
WECROSS_ROOT=${ROOT}/../

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

Download()
{
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -LO ${url}
    fi
}

prepare_bcos()
{
    cd ${ROOT}/bcos/
    # Download
    LOG_INFO "Download build_chain.sh ..."
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/tools/build_chain.sh
    chmod u+x build_chain.sh

    LOG_INFO "Download HelloWeCross.sol ..."
    cp ${WECROSS_ROOT}/src/main/resources/chains-sample/bcos/HelloWeCross.sol ./
    cd -
}

prepare_fabric()
{
    cd ${ROOT}/fabric/
    # Download
    LOG_INFO "Download fabric tools ..."
    Download https://github.com/hyperledger/fabric/releases/download/v1.4.6/hyperledger-fabric-darwin-amd64-1.4.6.tar.gz
    Download https://github.com/hyperledger/fabric/releases/download/v1.4.6/hyperledger-fabric-linux-amd64-1.4.6.tar.gz


    LOG_INFO "Download fabric samples ..."
    Download https://github.com/hyperledger/fabric-samples/archive/v1.4.4.tar.gz

    cd -
}

prepare_wecross()
{
    cd ${ROOT}
    LOG_INFO "Copy WeCross scripts"
    cp ${WECROSS_ROOT}/scripts/build_wecross.sh ./
    cp ${WECROSS_ROOT}/scripts/download_wecross.sh ./
    cp ${WECROSS_ROOT}/scripts/download_console.sh ./
}

main()
{
    prepare_bcos
    prepare_fabric
    prepare_wecross
}

main

