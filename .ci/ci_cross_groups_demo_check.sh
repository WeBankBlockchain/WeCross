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

cross_roup_demo_test()
{
    cd ${ROOT}

    bash build_cross_group.sh n

    cd WeCross-Console/
    bash start.sh <<EOF
listResources
listAccounts
call payment.group1.HelloWorldGroup1 bcos_user1 get
sendTransaction payment.group1.HelloWorldGroup1 bcos_user1 set Tom
call payment.group1.HelloWorldGroup1 bcos_user1 get
call payment.group2.HelloWorldGroup2 bcos_user1 get
sendTransaction payment.group2.HelloWorldGroup2 bcos_user1 set Jerry
call payment.group2.HelloWorldGroup2 bcos_user1 get
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


main()
{
    prepare_wecross
    prepare_wecross_console
    prepare_demo
    cross_roup_demo_test
}

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main