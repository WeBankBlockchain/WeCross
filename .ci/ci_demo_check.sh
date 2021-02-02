#!/bin/bash

set -e
ROOT=$(pwd)/demo/
PLUGIN_BRANCH=master

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

check_log() {
    cd ${ROOT}
    error_log=routers-payment/127.0.0.1-8250-25500/logs/error.log
    LOG_INFO "Check log ${error_log}"
    if [ "$(grep ERROR ${error_log} | wc -l)" -ne "0" ]; then
        cat ${error_log}
        LOG_ERROR "Error log is ${error_log}"
        exit 1
    fi

    error_log=routers-payment/127.0.0.1-8251-25501/logs/error.log
    LOG_INFO "Check log ${error_log}"
    if [ "$(grep ERROR ${error_log} | wc -l)" -ne "0" ]; then
        cat ${error_log}
        LOG_ERROR "Error log is ${error_log}"
        exit 1
    fi
}

check_console_log() {
    local log_file=$1

    if [ "$(grep TxError ${log_file} | wc -l)" -ne "0" ]; then
        grep TxError ${log_file}
        LOG_ERROR "Console TxError log is ${log_file}"
        exit 1
    fi
}

ensure_bcos_nodes_running() {
    bash ${ROOT}/bcos/nodes/127.0.0.1/start_all.sh
}

prepare_demo() {
    cd ${ROOT}

    bash .prepare.sh # prepare requirements

    cd -
}

demo_test() {
    cd ${ROOT}

    bash build.sh -H 127.0.0.1 -P 3306 -u root -p 123456

    ensure_bcos_nodes_running

    cd WeCross-Console/
    bash start.sh <<EOF
listResources
login
listAccount
listResources
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
    check_console_log ${ROOT}/WeCross-Console/logs/error.log

}

# htlc test
htlc_test() {
    cd ${ROOT}

    bash -x htlc_config.sh

    sleep 10

    ensure_bcos_nodes_running

    cd WeCross-Console/
    bash start.sh <<EOF
login org1-admin 123456
listAccount
listResources
setDefaultAccount Fabric1.4 1
call payment.fabric.htlc balanceOf User1@org2.example.com
newHTLCProposal payment.fabric.htlc bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 null false 0x4305196480b029bbecb071b4b68e95dfef36a7b7 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org2.example.com 500 2000000000
quit
EOF
    cd ..

    cd WeCross-Console/
    bash start.sh <<EOF
login org2-admin 123456
listAccount
listResources
call payment.bcos.htlc balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
newHTLCProposal payment.bcos.htlc bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 9dda9a5e175a919ee98ff0198927b0a765ef96cf917144b589bb8e510e04843c true 0x4305196480b029bbecb071b4b68e95dfef36a7b7 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org2.example.com 500 2000000000
quit
EOF
    cd ..

    sleep 20

    cd WeCross-Console/
    bash start.sh <<EOF
login org2-admin 123456
listAccount
listResources
call payment.bcos.htlc balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
quit
EOF
    cd ..

    cd WeCross-Console/
    bash start.sh <<EOF
login org1-admin 123456
listAccount
listResources
call payment.fabric.htlc balanceOf User1@org2.example.com
quit
EOF
    cd ..

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log
    check_console_log ${ROOT}/WeCross-Console/logs/error.log
}

# xa test
xa_evidence_test() {
    cd ${ROOT}

    bash -x xa_config_evidence.sh n

    docker ps | grep evidence
    sleep 20
    docker ps | grep evidence

    cd WeCross-Console/
    bash start.sh <<EOF
login
listAccount
listResources
call payment.bcos.evidence queryEvidence evidence0
call payment.fabric.evidence queryEvidence evidence0

startTransaction payment.bcos.evidence payment.fabric.evidence
execTransaction payment.bcos.evidence newEvidence evidence0 "I'm Tom"
execTransaction payment.fabric.evidence newEvidence evidence0 "I'm Jerry"
commitTransaction payment.bcos.evidence payment.fabric.evidence

call payment.bcos.evidence queryEvidence evidence0
call payment.fabric.evidence queryEvidence evidence0

startTransaction payment.bcos.evidence payment.fabric.evidence
execTransaction payment.bcos.evidence newEvidence evidence1 "I'm TomGG"
execTransaction payment.fabric.evidence newEvidence evidence1 "I'm JerryMM"
callTransaction payment.fabric.evidence queryEvidence evidence1
callTransaction payment.bcos.evidence queryEvidence evidence1
rollbackTransaction payment.bcos.evidence payment.fabric.evidence

call payment.bcos.evidence queryEvidence evidence1
call payment.fabric.evidence queryEvidence evidence1

quit
EOF

    cd -

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log
    check_console_log ${ROOT}/WeCross-Console/logs/error.log
}

xa_asset_test() {
    cd ${ROOT}

    bash -x xa_config_asset.sh n

    docker ps | grep asset
    sleep 20
    docker ps | grep asset

    cd WeCross-Console/
    bash start.sh <<EOF
login
listAccount
listResources
call payment.bcos.asset balanceOf Alice
call payment.fabric.asset balanceOf Alice

startTransaction payment.bcos.asset payment.fabric.asset
execTransaction payment.bcos.asset transfer Alice Oscar 100
execTransaction payment.fabric.asset transfer Alice Oscar 100
commitTransaction payment.bcos.asset payment.fabric.asset

call payment.bcos.asset balanceOf Alice
call payment.fabric.asset balanceOf Alice

startTransaction payment.bcos.asset payment.fabric.asset
execTransaction payment.bcos.asset transfer Alice Oscar 100
execTransaction payment.fabric.asset transfer Alice Oscar 100
rollbackTransaction payment.bcos.asset payment.fabric.asset

call payment.bcos.asset balanceOf Alice
call payment.fabric.asset balanceOf Alice

quit
EOF

    cd -

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log
    check_console_log ${ROOT}/WeCross-Console/logs/error.log
}

prepare_wecross() {
    ./gradlew assemble
    cd dist
    LOG_INFO "Download plugin from branch: ${PLUGIN_BRANCH}"
    bash download_plugin.sh BCOS2 ${PLUGIN_BRANCH}
    bash download_plugin.sh Fabric1 ${PLUGIN_BRANCH}
    bash download_pages.sh ${PLUGIN_BRANCH}
    cd -

    mv dist demo/WeCross
}

prepare_wecross_console() {
    cd ${ROOT}/
    LOG_INFO "Download wecross console from branch: ${PLUGIN_BRANCH}"
    bash WeCross/download_console.sh -s -t ${PLUGIN_BRANCH}
    cd -
}

prepare_account_manager() {
    cd ${ROOT}/
    LOG_INFO "Download wecross account manager from branch: ${PLUGIN_BRANCH}"
    bash -x WeCross/download_account_manager.sh -d -s -t ${PLUGIN_BRANCH}
    cd -
}

prepare_bcos() {
    cd ${ROOT}/bcos/
    echo "127.0.0.1:2 agency1 1" >ipconf
    cd -
}

main() {
    prepare_wecross
    prepare_wecross_console
    prepare_account_manager
    prepare_bcos
    prepare_demo
    demo_test
    htlc_test
    xa_evidence_test
    xa_asset_test
}

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main
