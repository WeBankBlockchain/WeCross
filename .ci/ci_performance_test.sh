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

sed_i()
{
    if [ "$(uname)" == "Darwin" ]; then
    # Mac
        sed -i "" $@
    else
        sed -i $@
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
call payment.fabric.abac fabric_user1 query a
sendTransaction payment.fabric.abac fabric_user1 invoke a b 10
call payment.fabric.abac fabric_user1 query a
call payment.fabric.abac fabric_user1 query b
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


performance_test_8250()
{
    cd ${ROOT}/WeCross-Console/
    cat conf/application.toml

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest bcos_user1 call 1000 500 1000 > bcos_local_call.txt
    cat bcos_local_call.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest bcos_user1 sendTransaction 1000 500 1000 > bcos_local_sendtx.txt
    cat bcos_local_sendtx.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.Fabric.FabricPerformanceTest fabric_user1 call 1000 500 1000 > fabric_remote_call.txt
    cat fabric_remote_call.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.Fabric.FabricPerformanceTest fabric_user1 sendTransaction 1000 500 1000 > fabric_remote_sendtx.txt
    cat fabric_remote_sendtx.txt
}

performance_test_8251()
{
    cd ${ROOT}/WeCross-Console/
    sed_i "s/8250/8251/g" conf/application.toml
    cat conf/application.toml

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest bcos_user1 call 1000 500 1000 > bcos_remote_call.txt
    cat bcos_remote_call.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest bcos_user1 sendTransaction 1000 500 1000 > bcos_remote_sendtx.txt
    cat bcos_remote_sendtx.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.Fabric.FabricPerformanceTest fabric_user1 call 1000 500 1000 > fabric_local_call.txt
    cat fabric_local_call.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.Fabric.FabricPerformanceTest fabric_user1 sendTransaction 1000 500 1000 > fabric_local_sendtx.txt
    cat fabric_local_sendtx.txt
}

prepare_performance_test()
{
    local fabric_router_dir=${ROOT}/routers-payment/127.0.0.1-8251-25501/

    bash ${ROOT}/fabric/deploy_sacc.sh

    cd ${fabric_router_dir}
    bash stop.sh
    cat >>conf/chains/fabric/stub.toml<<EOF

[[resources]]
    # name cannot be repeated
    name = 'abac'
    type = 'FABRIC_CONTRACT'
    chainCodeName = 'mycc'
    chainLanguage = 'go'
    peers=['org1','org2']
EOF
    bash start.sh
    cd -
}

performance_test()
{
    performance_test_8250
    performance_test_8251
}

main()
{
    prepare_wecross
    prepare_demo
    demo_test
    update_wecross_sdk
    prepare_performance_test
    performance_test
}

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main