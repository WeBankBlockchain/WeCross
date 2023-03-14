#!/bin/bash
set -e

LANG=en_US.UTF-8

default_compatibility_version=v1.3.0 # update this every release
BCOS_VERSION=v2.7.2  # use this version to specify get_account script

compatibility_version=
enable_build_from_resource=0

src_dir=$(pwd)'/src/'
GIT_URL_BASE='github.com'

wecross_console_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-Console.git
wecross_console_url_bak=https://gitee.com/Webank/WeCross-Console.git
wecross_console_branch=${default_compatibility_version}

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -#LO ${url}
    fi
}

help() {
    echo "$1"
    cat <<EOF
Usage:
    -s                              [Optional] Get wecross console by: gradle build from github Source Code.
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
            wecross_console_branch=$OPTARG
            compatibility_version=$OPTARG
            ;;
        t)
            wecross_console_branch=$OPTARG
            compatibility_version=$OPTARG
            ;;
        h) help ;;
        esac
    done

}

download_wecross_console_pkg() {
    local github_url=https://${GIT_URL_BASE}/WebankBlockchain/WeCross-Console/releases/download/
    local cdn_url=https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeCross/WeCross-Console/
    local release_pkg=WeCross-Console.tar.gz
    local release_pkg_checksum_file=WeCross-Console.tar.gz.md5

    if [ -d WeCross-Console/apps ]; then
        LOG_INFO "./WeCross-Console/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ]; then
        compatibility_version=$(curl -s https://api.${GIT_URL_BASE}/repos/WebankBlockchain/WeCross-Console/releases/latest | grep "tag_name" | awk -F '\"' '{print $4}')
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
    LOG_INFO "Build WeCross Console from source"

    local url=${wecross_console_url}
    local url_bak=${wecross_console_url_bak}
    local branch=${wecross_console_branch}
    local output_dir=$(pwd)

    if [ -d WeCross-Console ]; then
        LOG_INFO "./WeCross-Console/ exists"
        return
    fi

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross-Console ${url} ${url_bak} ${branch}

    cd WeCross-Console
    rm -rf dist
    bash ./gradlew assemble
    chmod +x dist/apps/*
    cd ..

    mv WeCross-Console/dist ${output_dir}/WeCross-Console
    chmod +x ${output_dir}/WeCross-Console/*.sh

    cd ${output_dir}

    LOG_INFO "Build WeCross Console successfully"
}

download_get_account_scripts() {
    local scripts_dir=${src_dir}/scripts
    mkdir -p ${scripts_dir}

    # download
    cd ${scripts_dir}

    LOG_INFO "Download get_account.sh ..."
    Download https://gitee.com/FISCO-BCOS/console/raw/${BCOS_VERSION}/tools/get_account.sh
    chmod u+x get_account.sh

    LOG_INFO "Download get_gm_account.sh ..."
    Download https://gitee.com/FISCO-BCOS/console/raw/${BCOS_VERSION}/tools/get_gm_account.sh
    chmod u+x get_gm_account.sh

    cd -

    # deploy
    local accounts_dir=WeCross-Console/conf/accounts/
    mkdir -p ${accounts_dir}
    cp ${scripts_dir}/get_account.sh ${accounts_dir}
    cp ${scripts_dir}/get_gm_account.sh ${accounts_dir}
}

main() {
    if [ 1 -eq ${enable_build_from_resource} ]; then
        build_from_source
        download_get_account_scripts
    else
        download_wecross_console_pkg
    fi
}

print_result() {
    LOG_INFO "Download completed. WeCross Console is in: ./WeCross-Console/"
    LOG_INFO "Please configure \"./WeCross-Console/conf/application.toml\" according with \"application-sample.toml\" and \"bash start.sh\" to start."
}

parse_command "$@"
main
print_result
