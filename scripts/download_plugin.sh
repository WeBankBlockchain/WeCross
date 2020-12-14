#!/bin/bash
set -e

LANG=en_US.UTF-8

enable_build_from_resource=0

deps_dir=$(pwd)'/plugin/'
src_dir=$(pwd)'/src/'

bcos_stub_url=https://github.com/WebankBlockchain/WeCross-BCOS2-Stub.git
bcos_stub_branch=

fabric_stub_url=https://github.com/WebankBlockchain/WeCross-Fabric1-Stub.git
fabric_stub_branch=

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
Download certain plugin to the dir: plugin/
Usage:
    bash $0 <name>  <tag/branch>

    <name>:         BCOS2   -> Repo: WeCross-BCOS2-Stub   ( BCOS2.0 & GM_BCOS2.0 )
                    Fabric1 -> Repo: WeCross-Fabric1-Stub ( Fabric1.4 )

    <tag/branch>:   certain tag or branch to download
e.g
    bash $0 BCOS2 v1.0.0
    bash $0 Fabric1 v1.0.0
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

build_plugin_from_source() {
    local name=${1}
    local url=${2}
    local branch=${3}
    local origin_dir=$(pwd)

    LOG_INFO "Build ${name} from source"

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code ${name} ${url} ${branch}

    cd ${name}
    bash ./gradlew assemble 2>&1 | tee output.log
    chmod +x dist/apps/*
    cd ..

    mkdir -p ${deps_dir}/

    cp ${name}/dist/apps/* ${deps_dir}/

    cd ${origin_dir}

    LOG_INFO "Download ${name} success!"
}

main() {
    local name=${1}
    local tag=${2}
    case ${name} in
    BCOS2)
        build_plugin_from_source WeCross-BCOS2-Stub ${bcos_stub_url} ${tag}
        ;;
    Fabric1)
        build_plugin_from_source WeCross-Fabric1-Stub ${fabric_stub_url} ${tag}
        ;;
    *)
        LOG_ERROR "Unsupported plugin name: "
        ;;
    esac
}

if [ $# != 2 ]; then
    help
    exit 0
fi

main $@
