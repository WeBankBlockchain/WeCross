#!/bin/bash

set -e
ROOT=$(pwd)/demo/
PLUGIN_BRANCH=master
OUTPUT_DIR=$(pwd)/.github/workflows/

LOG_INFO()
{
    echo -e "\033[32m[INFO] $@\033[0m"
}

LOG_ERROR()
{
    echo -e "\033[31m[ERROR] $@\033[0m"
}

pr_comment_file()
{
    local content="$(cat ${1}|sed ':label;N;s/\n/\\n/g;b label')"
    curl -H "Authorization: token ${GITHUB_TOKEN}" -X POST -d "{\"body\": \"${content}\"}" "https://api.github.com/repos/${TRAVIS_REPO_SLUG}/issues/${TRAVIS_PULL_REQUEST}/comments"
}

pr_comment_file_github()
{
    local content="$(cat ${1}|sed ':label;N;s/\n/\\n/g;b label')"
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
listAccount
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


txt_to_markdown()
{
    local txt_file_name=${1}
    local md_file_name=$(echo ${txt_file_name} | cut -d . -f1).md
    cat << EOF > ${md_file_name}
\`\`\`
Enviroment: github action machine
$(cat ${txt_file_name})
\`\`\`
EOF
}

publish_test_result()
{
    local txt_file=${1}
    local md_file=$(echo ${txt_file} | cut -d . -f1).md
    txt_to_markdown ${txt_file}
    cat ${md_file}
    cp ${md_file} ${OUTPUT_DIR}/
    #pr_comment_file_github ${md_file}
    pr_comment_file ${md_file}
}

performance_test_bcos_local()
{
    cd ${ROOT}/WeCross-Console/
    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest payment.bcos.HelloWeCross bcos_user1 call 20000 1000 500 > bcos_local_call.txt
    publish_test_result bcos_local_call.txt

    java -cp conf/:lib/*:apps/* com.webank.wecrosssdk.performance.BCOS.BCOSPerformanceTest payment.bcos.HelloWeCross bcos_user1 sendTransaction 10000 400 500 > bcos_local_sendtx.txt
    publish_test_result bcos_local_sendtx.txt
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

if [ -n "${TRAVIS_BRANCH}" ]; then
    PLUGIN_BRANCH=${TRAVIS_BRANCH}
fi

if [ -n "${1}" ]; then
    PLUGIN_BRANCH=${1}
fi

main