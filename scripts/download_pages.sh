#!/bin/bash
set -e

LANG=en_US.UTF-8


pages_dir=$(pwd)'/pages/'
src_dir=$(pwd)'/src/'
GIT_URL_BASE='github.com'

version_file="profile_version.sh"
[[ -f "${version_file}" ]] && {
  source "${version_file}"
}

wecross_webapp_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-WebApp.git
wecross_webapp_url_bak=https://gitee.com/WeBank/WeCross-WebApp.git

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
    bash $0 v1.3.0
EOF
    exit 0
}

download_latest_code() {
    local name=${1}
    local url=${2}
    local url_bak=${3}
    local branch=${4}

    if [ -d ${name} ]; then
        cd ${name}
        git checkout ${branch}
        git pull
        cd -
    else
        LOG_INFO "Try to clone from ${url}"
        if ! git clone --depth 1 -b ${branch} ${url}; then
            LOG_INFO "Try to clone from ${url_bak}"
            git clone --depth 1 -b ${branch} ${url_bak}
        fi
    fi
}

build_webapp_from_source() {
    LOG_INFO "Build WeCross WebApp from source"

    local url=${wecross_webapp_url}
    local url_bak=${wecross_webapp_url_bak}
    local branch=${1}

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross-WebApp ${url} ${url_bak} ${branch}

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

main "$@"
