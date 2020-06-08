#!/bin/bash
set -e
LANG=en_US.utf8

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO][FISCO BCOS] ${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m[ERROR][FISCO BCOS] ${content}\033[0m"
}

Download()
{
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -LO ${url}
    fi
}

build_bcos_chain()
{
    # Download
    LOG_INFO "Download build_chain.sh ..."
    Download https://github.com/FISCO-BCOS/FISCO-BCOS/raw/master/tools/build_chain.sh
    chmod u+x build_chain.sh

    LOG_INFO "Download HelloWeCross.sol ..."
    Download https://github.com/WeBankFinTech/WeCross/releases/download/resources/HelloWeCross.sol

    # Build chain
    LOG_INFO "Build chain ..."
    echo "127.0.0.1:4 agency1 1" >ipconf

    # build chain
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        if [ -e fisco-bcos-mac ];then
            ./build_chain.sh -f ipconf -p 30300,20200,8545 -e ./fisco-bcos-mac
        else
            ./build_chain.sh -f ipconf -p 30300,20200,8545
        fi
    else
        # Other
        if [ -e fisco-bcos ];then
            ./build_chain.sh -f ipconf -p 30300,20200,8545 -e ./fisco-bcos
        else
            ./build_chain.sh -f ipconf -p 30300,20200,8545
        fi
    fi

    ./nodes/127.0.0.1/start_all.sh
}


build_console()
{
    # Download console
    LOG_INFO "Download console ..."
    bash ./nodes/127.0.0.1/download_console.sh -v 1.0.9

    # Copy demo HelloWeCross
    cp HelloWeCross.sol console/contracts/solidity/

    # Deploy contract
    LOG_INFO "Deploy contract ..."
    cp -n console/conf/applicationContext-sample.xml console/conf/applicationContext.xml
    cp nodes/127.0.0.1/sdk/* console/conf/
}

deploy_contract()
{
    cd console
    bash start.sh <<EOF
    deploy HelloWeCross
EOF
    hello_address=$(grep 'HelloWeCross' deploylog.txt | awk '{print $5}')
    cd -
}

main()
{
    build_bcos_chain
    build_console
    deploy_contract
}

main
