#!/bin/bash

set -e
ROOT=$(pwd)/demo/
PLUGIN_BRANCH=master

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

check_log()
{
    cd ${ROOT}
    error_log=routers-payment/127.0.0.1-8250-25500/logs/error.log
    LOG_INFO "Check log ${error_log}"
    if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
        cat ${error_log}
        LOG_ERROR "Error log is ${error_log}"
        exit 1
    fi

    error_log=routers-payment/127.0.0.1-8251-25501/logs/error.log
    LOG_INFO "Check log ${error_log}"
    if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
        cat ${error_log}
        LOG_ERROR "Error log is ${error_log}"
        exit 1
    fi
}

prepare_demo()
{
    cd ${ROOT}

    bash .prepare.sh # prepare requirements

    cd -
}

demo_test()
{
    cd ${ROOT}

    bash build.sh n

    cd WeCross-Console/
    bash start.sh <<EOF
listResources
listAccounts
call payment.bcos.HelloWeCross bcos_user1 get
sendTransaction payment.bcos.HelloWeCross bcos_user1 set Tom
call payment.fabric.mycc fabric_user1 query a
sendTransaction payment.fabric.mycc fabric_user1 invoke a b 10
call payment.fabric.mycc fabric_user1 query a
call payment.fabric.mycc fabric_user1 query b
quit
EOF
    cd ..

    check_log
}

# htlc test
htlc_test()
{
    cd ${ROOT}

    bash htlc_config.sh

    cd WeCross-Console/
    bash start.sh <<EOF
call payment.bcos.LedgerSampleHTLC bcos_sender balanceOf ["0x2b5ad5c4795c026514f8317c7a215e218dccd6cf"]
newHTLCProposal payment.bcos.LedgerSampleHTLC bcos_sender ["bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11","9dda9a5e175a919ee98ff0198927b0a765ef96cf917144b589bb8e510e04843c","true","0x55f934bcbe1e9aef8337f5551142a442fdde781c","0x2b5ad5c4795c026514f8317c7a215e218dccd6cf","700","2000000000","Admin@org1.example.com","User1@org1.example.com","500","2000010000"]
quit
EOF
    cd ..

    cd WeCross-Console-8251/
    bash start.sh <<EOF
call payment.fabric.LedgerSampleHTLC fabric_admin balanceOf User1@org1.example.com
newHTLCProposal payment.fabric.LedgerSampleHTLC fabric_admin bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 null false 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000000000 Admin@org1.example.com User1@org1.example.com 500 2000010000
quit
EOF
    cd ..

    sleep 30

    cd WeCross-Console/
    bash start.sh <<EOF
call payment.bcos.LedgerSampleHTLC bcos_sender balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
quit
EOF
    cd ..

    cd WeCross-Console-8251/
    bash start.sh <<EOF
call payment.fabric.LedgerSampleHTLC fabric_admin balanceOf User1@org1.example.com
quit
EOF
    cd ..

    check_log
}

prepare_wecross()
{
    ./gradlew assemble
    cd dist
    LOG_INFO "Download plugin from branch: ${PLUGIN_BRANCH}"
    bash download_plugin.sh BCOS2 ${PLUGIN_BRANCH}
    bash download_plugin.sh Fabric1 ${PLUGIN_BRANCH}
    cd -

    mv dist demo/WeCross
}


prepare_wecross_console()
{
    cd ${ROOT}/
    LOG_INFO "Download wecross console from branch: ${PLUGIN_BRANCH}"
    bash WeCross/download_console.sh -s -t ${PLUGIN_BRANCH}
    cd -
}

prepare_bcos()
{
    cd ${ROOT}/bcos/
    echo "127.0.0.1:2 agency1 1" > ipconf
    cd -
}

prepare_htlc()
{
    cd ${ROOT}/bcos/
    LOG_INFO "Download ledger-tool from branch: ${PLUGIN_BRANCH}"
    git clone --depth 1 -b ${PLUGIN_BRANCH} https://github.com/Shareong/ledger-tool.git
    cd ledger-tool
    ./gradlew assemble
    mv dist ledger-tool
    tar -zcf ledger-tool.tar.gz ledger-tool
    mv ledger-tool.tar.gz ${ROOT}/bcos/
    cd ${ROOT}/bcos/
    rm -rf ledger-tool
    cd ${ROOT}
}

main()
{
    prepare_wecross
    prepare_wecross_console
    prepare_bcos
    # prepare_htlc
    prepare_demo
    demo_test
    # htlc_test
}

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main