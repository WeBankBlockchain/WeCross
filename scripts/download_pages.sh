#!/bin/bash
set -e

LANG=en_US.UTF-8


pages_dir=$(pwd)'/pages/'
src_dir=$(pwd)'/src/'

wecross_webapp_url=https://github.com/WeBankFinTech/WeCross-WebApp.git

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

help() {
    echo "$1"
    cat <<EOF
Download WeCross-WebApp to dir: pages/
Usage:
    bash $0  <tag/branch>
    <tag/branch>:   certain tag or branch to download
e.g
    bash $0 v1.0.0
EOF
    exit 0
}

download_latest_code() {
    local name=${1}
    local url=${2}
    local branch=${3}

    if [ -d ${name} ]; then
        cd ${name}
        git checkout ${branch}
        git pull
        cd -
    else
        git clone --depth 1 -b ${branch} ${url}

    fi
}

build_webapp_from_source() {
    LOG_INFO "Build WeCross WebApp from source"

    local url=${wecross_webapp_url}
    local branch=${1}

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross-WebApp ${url} ${branch}

    cd WeCross-WebApp
    rm -rf dist
    npm install

    if ! npm run build:prod ; then
        LOG_ERROR "Build Wecross Console project failed"
        exit 1
    fi
    echo "================================================================"


    mkdir -p ${pages_dir}
    cp -r dist/* ${pages_dir}/

    cd -

    LOG_INFO "Build WeCross Console successfully"
}

main() {
    local tag=${1}
    build_webapp_from_source ${tag}
}

if [ $# != 1 ]; then
    help
    exit 0
fi

main $@
