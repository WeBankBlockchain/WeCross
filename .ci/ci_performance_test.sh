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

pr_comment()
{
    local content=${1}
    local pr_number=$(jq --raw-output .pull_request.number "$GITHUB_EVENT_PATH")
    LOG_INFO "PR: ${pr_number}"
    LOG_INFO "GITHUB_REPOSITORY: ${GITHUB_REPOSITORY}"
    curl -H "Authorization: token ${GITHUB_TOKEN}" -X POST -d "{\"body\": \"${content}\"}" "https://api.github.com/repos/${GITHUB_REPOSITORY}/issues/${pr_number}/comments"
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

cross_group_demo_test()
{
    cd ${ROOT}

    bash build_single_bcos.sh n

    cd WeCross-Console/
    bash start.sh <<EOF
listResources
listAccounts
call payment.bcos.HelloWeCross bcos_user1 get
sendTransaction payment.bcos.HelloWeCross bcos_user1 set ["Tom"]
call payment.bcos.HelloWeCross bcos_user1 get
quit
EOF
    cd ..

    check_log
}

demo_test()
{
    cross_group_demo_test
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

update_wecross_sdk()
{
    local dest_dir=${ROOT}/WeCross-Console/lib/

    mkdir -p ${ROOT}/src
    cd ${ROOT}/src
    git clone --depth 1 -b ${PLUGIN_BRANCH} https://github.com/WeBankFinTech/WeCross-Java-SDK.git
    cd WeCross-Java-SDK
    bash gradlew assemble

    rm ${dest_dir}/wecross-java-sdk*
    cp dist/apps/* ${dest_dir}/
    cd ${ROOT}
}


performance_test_bcos_local()
{
    cd ${ROOT}/WeCross-Console/
    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest payment.bcos.HelloWeCross bcos_user1 call 1000 500 1000 > bcos_local_call.txt
    cat bcos_local_call.txt
    #pr_comment "$(cat bcos_local_call.txt)"

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest payment.bcos.HelloWeCross bcos_user1 sendTransaction 1000 500 1000 > bcos_local_sendtx.txt
    cat bcos_local_sendtx.txt
    #pr_comment "$(cat bcos_local_sendtx.txt)"

}


performance_test()
{
    performance_test_bcos_local
}

main()
{
    LOG_INFO "Run with branch: " ${PLUGIN_BRANCH}
    prepare_wecross
    prepare_wecross_console
    prepare_demo
    demo_test
    update_wecross_sdk
    performance_test
}

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main