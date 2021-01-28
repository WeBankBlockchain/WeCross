#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}

set -e
LANG=en_US.UTF-8

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO][FISCO BCOS] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR][FISCO BCOS] ${content}\033[0m"
}

version_file="../profile_version.sh"
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
        curl -#LO ${url}
    fi
}

build_bcos_chain() {
    # Download
    LOG_INFO "Download build_chain.sh ..."
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${BCOS_VERSION}/build_chain.sh
    chmod u+x build_chain.sh

    local support_version="$1"
    local support_version_cmd=""
    if [[ -n ${support_version} ]]; then
        LOG_INFO "Support version: ${support_version} "
        support_version_cmd="-v ${support_version}"
    fi

    # Build chain
    LOG_INFO "Build chain ..."
    if [ ! -e ipconf ]; then
        echo "127.0.0.1:4 agency1 1" >ipconf
    fi

    # build chain
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        if [ -e fisco-bcos-macOS.tar.gz ]; then
            rm -f ./fisco-bcos
            tar -zxvf fisco-bcos-macOS.tar.gz
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -e ./fisco-bcos -g -G -o nodes_gm "${support_version_cmd}"
        else
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -g -G -o nodes_gm "${support_version_cmd}"
        fi
    else
        # Other
        if [ -e fisco-bcos.tar.gz ]; then
            rm -f ./fisco-bcos
            tar -zxvf fisco-bcos.tar.gz
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -e ./fisco-bcos -g -G -o nodes_gm "${support_version_cmd}"
        else
            ./build_chain.sh -f ipconf -p 30310,20210,8555 -g -G -o nodes_gm "${support_version_cmd}"
        fi
    fi

    ./nodes_gm/127.0.0.1/start_all.sh
}

build_accounts() {
    LOG_INFO "Download get_gm_account.sh ..."
    Download https://raw.githubusercontent.com/FISCO-BCOS/console/master/tools/get_gm_account.sh
    chmod u+x get_gm_account.sh

    # generate accounts
    mkdir -p accounts
    cd accounts

    bash ../get_gm_account.sh # gm
    mv accounts_gm bcos_gm_user1
    cd -
}

main() {
    build_bcos_chain "$1"
    build_accounts
    LOG_INFO "SUCCESS: Build FISCO BCOS Guomi demo finish."
}

main "$1"
