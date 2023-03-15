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
LOG_INFO "WeCross Version: ${WECROSS_VERSION}"
LOG_INFO "BCOS Version: ${BCOS3_VERSION}"

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -#LO ${url}
    fi
}

build_bcos_chain() {
    if [ ! -e build_chain.sh ]; then
        # Download
        LOG_INFO "Download build_chain.sh ..."
        Download https://${GIT_URL_BASE}/FISCO-BCOS/FISCO-BCOS/releases/download/${BCOS3_VERSION}/build_chain.sh
    fi

    chmod u+x build_chain.sh

    # Build chain
    LOG_INFO "Build chain ..."
    # Setting to build 1 groups
    bash build_chain.sh -p 30400,20300 -l 127.0.0.1:1 -o ./nodes

    ./nodes/127.0.0.1/start_all.sh
}

build_accounts() {
    if [ ! -e get_account.sh ]; then
        # Download
        LOG_INFO "Download get_account.sh ..."
        Download "https://${GITHUB_PROXY}raw.githubusercontent.com/FISCO-BCOS/console/${BCOS_VERSION}/tools/get_account.sh"
    fi

    chmod u+x get_account.sh

    # generate accounts
    mkdir -p accounts
    cd accounts

    bash ../get_account.sh # normal
    mv accounts bcos_user1
    cd -
}

main() {
    build_bcos_chain "$1"
    build_accounts
    LOG_INFO "SUCCESS: Build FISCO BCOS demo finish."
}

main "$1"
