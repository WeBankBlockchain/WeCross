#!/bin/bash
set -e

LANG=en_US.UTF-8

default_compatibility_version=v1.0.1 # update this every release

compatibility_version=

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
Usage: Download WeCross demo
    -t  <tag name>                      [Optional] download demo from a given tag
    -h                                  call for help
e.g
    bash $0
EOF
    exit 0
}

parse_command() {
    while getopts "t:h" option; do
        # shellcheck disable=SC2220
        case ${option} in
        t)
            compatibility_version=$OPTARG
            ;;
        h) help ;;
        esac
    done

}

download_demo() {
    local github_url=https://github.com/WebankBlockchain/WeCross/releases/download/
    local cdn_url=https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeCross/Demo/
    #local compatibility_version=
    local release_pkg=demo.tar.gz
    local release_pkg_checksum_file=demo.tar.gz.md5

    if [ -d ./demo/ ]; then
        LOG_INFO "./demo/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ]; then
        compatibility_version=$(curl -s https://api.github.com/repos/WebankBlockchain/WeCross/releases/latest | grep "tag_name" | awk -F '\"' '{print $4}')
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

    mkdir -p ~/.wecross_pkg
    cd ~/.wecross_pkg

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

    cd -
    mv ~/.wecross_pkg/demo wecross-demo
}

main() {
    download_demo

}

print_result() {
    LOG_INFO "Download completed. WeCross Demo is in: ./wecross-demo/"
    LOG_INFO "Please: \"cd ./wecross-demo/ \" and \"bash build.sh\" to build the demo."
}

parse_command $@
main
print_result
