#!/bin/bash
set -e
LANG=en_US.utf8

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO][FISCO BCOS] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR][FISCO BCOS] ${content}\033[0m"
}

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -LO ${url}
    fi
}

build_bcos_chain() {
    # Download
    LOG_INFO "Download build_chain.sh ..."
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/tools/build_chain.sh
    chmod u+x build_chain.sh

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
            ./build_chain.sh -f ipconf -p 30300,20200,8545 -e ./fisco-bcos
        else
            ./build_chain.sh -f ipconf -p 30300,20200,8545
        fi
    else
        # Other
        if [ -e fisco-bcos.tar.gz ]; then
            rm -f ./fisco-bcos
            tar -zxvf fisco-bcos.tar.gz
            ./build_chain.sh -f ipconf -p 30300,20200,8545 -e ./fisco-bcos
        else
            ./build_chain.sh -f ipconf -p 30300,20200,8545
        fi
    fi

    ./nodes/127.0.0.1/start_all.sh
}

build_console() {
    # Download console
    LOG_INFO "Download HelloWeCross.sol ..."
    Download https://github.com/WeBankFinTech/WeCross/releases/download/resources/HelloWeCross.sol

    LOG_INFO "Download console ..."
    if [ -e console.tar.gz ]; then
        rm -rf console
        tar -zxvf console.tar.gz
    else
        bash ./nodes/127.0.0.1/download_console.sh -v 1.0.10
    fi

    # Copy demo HelloWeCross
    cp HelloWeCross.sol console/contracts/solidity/

    # Deploy contract
    LOG_INFO "Deploy contract ..."
    cp -n console/conf/applicationContext-sample.xml console/conf/applicationContext.xml
    cp nodes/127.0.0.1/sdk/* console/conf/
}

build_accounts() {
    LOG_INFO "Download get_account.sh ..."
    Download https://raw.githubusercontent.com/FISCO-BCOS/console/master/tools/get_account.sh
    chmod u+x get_account.sh
    # generate accounts
    mkdir -p accounts
    cd accounts

    bash ../get_account.sh # normal
    mv accounts bcos_user1
    cd -
}

deploy_contract() {
    cd console
    bash start.sh <<EOF
    deployByCNS HelloWeCross 1.0
EOF
    hello_address=$(grep 'HelloWeCross' deploylog.txt | awk '{print $5}')
    cd -
}

main() {
    build_bcos_chain
    build_accounts
    LOG_INFO "SUCCESS: Build FISCO BCOS demo finish."
}

main
