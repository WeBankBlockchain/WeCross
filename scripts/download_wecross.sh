#!/bin/bash
set -e

LANG=en_US.UTF-8

enable_build_from_resource=0
compatibility_version=

default_compatibility_version=v1.3.0 # update this every release
deps_dir=$(pwd)'/WeCross/plugin/'
pages_dir=$(pwd)'/WeCross/pages/'
src_dir=$(pwd)'/src/'
GIT_URL_BASE='github.com'

version_file="profile_version.sh"
[[ -f "${version_file}" ]] && {
  source "${version_file}"
}

wecross_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross.git
wecross_url_bak=https://gitee.com/Webank/WeCross.git
wecross_branch=${default_compatibility_version}

bcos_stub_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-BCOS2-Stub.git
bcos_stub_url_bak=https://gitee.com/Webank/WeCross-BCOS2-Stub.git
bcos_stub_branch=${default_compatibility_version}

bcos3_stub_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-BCOS3-Stub.git
bcos3_stub_url_bak=https://gitee.com/Webank/WeCross-BCOS3-Stub.git
bcos3_stub_branch=${default_compatibility_version}

fabric1_stub_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-Fabric1-Stub.git
fabric1_stub_url_bak=https://gitee.com/Webank/WeCross-Fabric1-Stub.git
fabric1_stub_branch=${default_compatibility_version}

fabric2_stub_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-Fabric2-Stub.git
fabric2_stub_url_bak=https://gitee.com/Webank/WeCross-Fabric2-Stub.git
fabric2_stub_branch=${default_compatibility_version}

wecross_webapp_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-WebApp.git
wecross_webapp_url_bak=https://gitee.com/Webank/WeCross-WebApp.git
wecross_webapp_branch=${default_compatibility_version}

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
Usage:
    -s                              [Optional] Get wecross by: gradle build from github Source Code.
    -b                              [Optional] Download from certain branch
    -t                              [Optional] Download from certain tag (same as -b)
    -h  call for help
e.g
    bash $0
    bash $0 -s
EOF
    exit 0
}

parse_command() {
    while getopts "b:t:sh" option; do
        # shellcheck disable=SC2220
        case ${option} in
        s)
            enable_build_from_resource=1
            ;;
        b)
            wecross_branch=$OPTARG
            bcos_stub_branch=$OPTARG
            bcos3_stub_branch=$OPTARG
            fabric1_stub_branch=$OPTARG
            fabric2_stub_branch=$OPTARG
            wecross_webapp_branch=$OPTARG
            compatibility_version=$OPTARG
            ;;
        t)
            wecross_branch=$OPTARG
            bcos_stub_branch=$OPTARG
            bcos3_stub_branch=$OPTARG
            fabric1_stub_branch=$OPTARG
            fabric2_stub_branch=$OPTARG
            wecross_webapp_branch=$OPTARG
            compatibility_version=$OPTARG
            ;;
        h) help ;;
        esac
    done

}

download_wecross_pkg() {
    local github_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross/releases/download/
    local cdn_url=https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeCross/WeCross/
    local release_pkg=WeCross.tar.gz
    local release_pkg_checksum_file=WeCross.tar.gz.md5

    if [ -d WeCross/apps ]; then
        LOG_INFO "./WeCross/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ]; then
        compatibility_version=$(curl -s https://api.${GIT_URL_BASE}/repos/WebankBlockchain/WeCross/releases/latest | grep "tag_name" | awk -F '\"' '{print $4}')
    fi

    if [ -z "${compatibility_version}" ]; then
        # could not get version from github
        compatibility_version=${default_compatibility_version}
    fi

    LOG_INFO "Latest release: ${compatibility_version}"

    download_release_pkg ${github_url} ${cdn_url} ${compatibility_version} ${release_pkg} ${release_pkg_checksum_file}
}

download_release_pkg() {
    local github_url=${1}
    local cdn_url=${2}
    local compatibility_version=${3}
    local release_pkg=${4}
    local release_pkg_checksum_file=${5}

    #download checksum
    LOG_INFO "Try to Download checksum from ${cdn_url}/${compatibility_version}/${release_pkg_checksum_file}"
    if ! curl --fail -#LO ${cdn_url}/${compatibility_version}/${release_pkg_checksum_file}; then
        LOG_INFO "Download checksum from ${github_url}/${compatibility_version}/${release_pkg_checksum_file}"
        curl -#LO ${github_url}/${compatibility_version}/${release_pkg_checksum_file}
    fi

    if [ ! -e ${release_pkg_checksum_file} ] || [ -z "$(grep ${release_pkg} ${release_pkg_checksum_file})" ]; then
        LOG_ERROR "Download checksum file error"
        exit 1
    fi

    # download
    if [ -f "${release_pkg}" ] && md5sum -c ${release_pkg_checksum_file}; then
        LOG_INFO "Latest release ${release_pkg} exists."
    else
        LOG_INFO "Try to download from: ${cdn_url}/${compatibility_version}/${release_pkg}"
        if ! curl --fail -#LO ${cdn_url}/${compatibility_version}/${release_pkg}; then
            # If CDN failed, download from github release
            LOG_INFO "Download from: ${github_url}/${compatibility_version}/${release_pkg}"
            curl -C - -#LO ${github_url}/${compatibility_version}/${release_pkg}
        fi

        if ! md5sum -c ${release_pkg_checksum_file}; then
            LOG_ERROR "Download package error"
            rm -f ${release_pkg}
            exit 1
        fi
    fi

    tar -zxf ${release_pkg}
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

build_from_source() {
    LOG_INFO "Build WeCross from source"

    local url=${wecross_url}
    local url_bak=${wecross_url_bak}
    local branch=${wecross_branch}
    local output_dir=$(pwd)

    if [ -d WeCross ]; then
        LOG_INFO "./WeCross/ exists"
        return
    fi

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross ${url} ${url_bak} ${branch}

    cd WeCross
    rm -rf dist
    bash ./gradlew assemble
    chmod +x dist/apps/*
    cd ..

    mv WeCross/dist ${output_dir}/WeCross

    chmod +x ${output_dir}/WeCross/*.sh

    cd ${output_dir}
}

build_plugin_from_source() {
    local name=${1}
    local url=${2}
    local url_bak=${3}
    local branch=${4}
    local origin_dir=$(pwd)

    LOG_INFO "Build ${name} from source"

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code ${name} ${url} ${url_bak} ${branch}

    cd ${name}
    bash ./gradlew assemble
    chmod +x dist/apps/*
    cd ..

    mkdir -p ${deps_dir}/

    cp ${name}/dist/apps/* ${deps_dir}/

    cd ${origin_dir}
}

build_webapp_from_source() {
    LOG_INFO "Build WeCross WebApp from source"

    local url=${wecross_webapp_url}
    local url_bak=${wecross_webapp_url_bak}
    local branch=${wecross_webapp_branch}

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross-WebApp ${url} ${url_bak} ${branch}

    cd WeCross-WebApp
    rm -rf dist
    npm install

    if ! npm run build:prod ; then
        LOG_ERROR "Build Wecross WebApp project failed"
        exit 1
    fi
    echo "================================================================"


    mkdir -p ${pages_dir}
    cp -r dist/* ${pages_dir}/

    cd -

    LOG_INFO "Build WeCross WebApp successfully"
}

main() {
    if [ 1 -eq ${enable_build_from_resource} ]; then
        build_from_source
        build_plugin_from_source WeCross-BCOS2-Stub ${bcos_stub_url} ${bcos_stub_url_bak} ${bcos_stub_branch}
        build_plugin_from_source WeCross-BCOS3-Stub ${bcos3_stub_url} ${bcos3_stub_url_bak} ${bcos3_stub_branch}
        build_plugin_from_source WeCross-Fabric1-Stub ${fabric1_stub_url} ${fabric1_stub_url_bak} ${fabric1_stub_branch}
        build_plugin_from_source WeCross-Fabric2-Stub ${fabric2_stub_url} ${fabric2_stub_url_bak} ${fabric2_stub_branch}
        build_webapp_from_source
    else
        download_wecross_pkg
    fi
}

print_result() {
    LOG_INFO "Download completed. WeCross is in: ./WeCross/"
}

parse_command "$@"
main
print_result
