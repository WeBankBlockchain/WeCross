#!/bin/bash

set -e
CI_PWD=$(pwd)
ROOT=$(pwd)/demo/
PLUGIN_BRANCH=master
final_rare_input=""

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

prepare_rare_string() {
  final_rare_input=$(bash ${CI_PWD}/.ci/gen_rare_string.sh)
}

check_rare_string() {
  bash ${CI_PWD}/.ci/check_rare_string.sh ${final_rare_input} ${ROOT}/WeCross-Console/logs/debug.log
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

    bash build_cross_fabric2.sh -H 127.0.0.1 -P 3306 -u root -p 123456

    ensure_bcos_nodes_running
    prepare_rare_string

    cd WeCross-Console/
    bash start.sh <<EOF
listResources
login
listAccount
listResources
call payment.bcos.HelloWorld get
sendTransaction payment.bcos.HelloWorld set Tom
call payment.bcos.HelloWorld get
sendTransaction payment.fabric2.sacc set a 666
call payment.fabric2.sacc query a
sendTransaction payment.fabric2.sacc set a ${final_rare_input}
call payment.fabric2.sacc query a
quit
EOF
    cd ..

    check_log
    check_console_log ${ROOT}/WeCross-Console/logs/warn.log
    check_console_log ${ROOT}/WeCross-Console/logs/error.log
    check_rare_string
}

prepare_wecross() {
    ./gradlew assemble
    cd dist
    LOG_INFO "Download plugin from branch: ${PLUGIN_BRANCH}"
    bash download_plugin.sh BCOS2 ${PLUGIN_BRANCH}
    bash download_plugin.sh Fabric2 ${PLUGIN_BRANCH}
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
}

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main
