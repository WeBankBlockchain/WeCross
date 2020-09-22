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

check_console_log()
{
    local log_file=$1

    if [ "$(grep TxError ${log_file} |wc -l)" -ne "0" ];then
        grep TxError ${log_file}
        LOG_ERROR "Console TxError log is ${log_file}"
        exit 1
    fi
}

ensure_bcos_nodes_running()
{
    bash ${ROOT}/bcos/nodes/127.0.0.1/start_all.sh
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

    ensure_bcos_nodes_running

    cd WeCross-Console/
    bash start.sh <<EOF
listResources
login
listAccount
call payment.bcos.HelloWorld get
sendTransaction payment.bcos.HelloWorld set Tom
call payment.bcos.HelloWorld get
call payment.fabric.sacc query a
sendTransaction payment.fabric.sacc set a 666
call payment.fabric.sacc query a
quit
EOF
    cd ..

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log

}

# htlc test
htlc_test()
{
    cd ${ROOT}

    bash -x htlc_config.sh

    sleep 10

    ensure_bcos_nodes_running

    cd WeCross-Console-8251/
    bash start.sh <<EOF
call payment.fabric.htlc fabric_admin balanceOf User1@org1.example.com
newHTLCProposal payment.fabric.htlc fabric_admin bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 null false 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org1.example.com 500 2000000000
quit
EOF
    cd ..

    cd WeCross-Console/
    bash start.sh <<EOF
call payment.bcos.htlc bcos_sender balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
newHTLCProposal payment.bcos.htlc bcos_sender bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 9dda9a5e175a919ee98ff0198927b0a765ef96cf917144b589bb8e510e04843c true 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org1.example.com 500 2000000000
quit
EOF
    cd ..

    sleep 20

    cd WeCross-Console/
    bash start.sh <<EOF
call payment.bcos.htlc bcos_sender balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
quit
EOF
    cd ..

    cd WeCross-Console-8251/
    bash start.sh <<EOF
call payment.fabric.htlc fabric_admin balanceOf User1@org1.example.com
quit
EOF
    cd ..

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log
    check_console_log ${ROOT}/WeCross-Console-8251/logs/warn.log
}

# 2pc test
2pc_test()
{
    cd ${ROOT}

    bash 2pc_config.sh n

    cd WeCross-Console/
    bash start.sh <<EOF
call payment.bcos.evidence bcos_user1 queryEvidence evidence0
call payment.fabric.evidence fabric_user1 queryEvidence evidence0

startTransaction 100 bcos_user1 fabric_user1 payment.bcos.evidence payment.fabric.evidence
execTransaction payment.bcos.evidence bcos_user1 100 1 newEvidence evidence0 "I'm Tom"
execTransaction payment.fabric.evidence fabric_user1 100 1 newEvidence evidence0 "I'm Jerry"
commitTransaction 100 bcos_user1 fabric_user1 payment.bcos.evidence payment.fabric.evidence

call payment.bcos.evidence bcos_user1 queryEvidence evidence0
call payment.fabric.evidence fabric_user1 queryEvidence evidence0

startTransaction 101 bcos_user1 fabric_user1 payment.bcos.evidence payment.fabric.evidence
execTransaction payment.bcos.evidence bcos_user1 101 1 newEvidence evidence1 "I'm TomGG"
execTransaction payment.fabric.evidence fabric_user1 101 1 newEvidence evidence1 "I'm JerryMM"
callTransaction payment.bcos.evidence bcos_user1 101 queryEvidence evidence1
callTransaction payment.fabric.evidence fabric_user1 101 queryEvidence evidence1
callTransaction payment.bcos.evidence bcos_user1 101 queryEvidence evidence1
rollbackTransaction 101 bcos_user1 fabric_user1 payment.bcos.evidence payment.fabric.evidence

call payment.bcos.evidence bcos_user1 queryEvidence evidence1
call payment.fabric.evidence fabric_user1 queryEvidence evidence1

quit
EOF

    cd -

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log
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

prepare_account_manager()
{
    cd ${ROOT}/
    LOG_INFO "Download wecross account manager from branch: ${PLUGIN_BRANCH}"
    bash WeCross/download_account_manager.sh -s -t ${PLUGIN_BRANCH}
    cd -
}

prepare_bcos()
{
    cd ${ROOT}/bcos/
    echo "127.0.0.1:2 agency1 1" > ipconf
    cd -
}

main()
{
    prepare_wecross
    prepare_wecross_console
    prepare_account_manager
    prepare_bcos
    prepare_demo
    demo_test
    # htlc_test
    # 2pc_test
}

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main