#!/bin/bash
set -e

LANG=en_US.utf8

default_compatibility_version=v1.0.0-rc2 # update this every release

compatibility_version=
enable_build_from_resource=0

src_dir=$(pwd)'/src/'

wecross_console_url=https://github.com/WeBankFinTech/WeCross-Console.git
wecross_console_branch=master

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

help()
{
    echo "$1"
    cat << EOF
Usage:
    -s                              [Optional] Get wecross console by: gradle build from github Source Code.
    -b                              [Optional] Download from certain branch if '-s' is set
    -h  call for help
e.g
    bash $0 
    bash $0 -s 
EOF
exit 0
}


parse_command()
{
while getopts "b:sh" option;do
    # shellcheck disable=SC2220
    case ${option} in
    s)
        enable_build_from_resource=1
    ;;
    b)
        wecross_console_branch=$OPTARG
    ;;
    h)  help;;
    esac
done

}

download_wecross_console_pkg()
{
    local github_url=https://github.com/WeBankFinTech/WeCross-Console/releases/download/
    local cdn_url=https://www.fisco.com.cn/cdn/wecross-console/releases/download/
    local compatibility_version=
    local release_pkg=WeCross-Console.tar.gz
    local release_pkg_checksum_file=WeCross-Console.tar.gz.md5

    if [ -d WeCross-Console/apps ];then
        LOG_INFO "./WeCross-Console/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ];then
        compatibility_version=$(curl -s https://api.github.com/repos/WeBankFinTech/WeCross-Console/releases/latest | grep "tag_name"|awk -F '\"' '{print $4}')
    fi

    if [ -z "${compatibility_version}" ];then
        # could not get version from github
        compatibility_version=${default_compatibility_version}
    fi

    LOG_INFO "Latest release: ${compatibility_version}"

    download_release_pkg ${github_url} ${cdn_url} ${compatibility_version} ${release_pkg} ${release_pkg_checksum_file}
}

download_release_pkg()
{
    local github_url=${1}
    local cdn_url=${2}
    local compatibility_version=${3}
    local release_pkg=${4}
    local release_pkg_checksum_file=${5}

    #download checksum
    LOG_INFO "Try to Download checksum from ${cdn_url}/${compatibility_version}/${release_pkg_checksum_file}"
    if ! curl --fail -LO ${cdn_url}/${compatibility_version}/${release_pkg_checksum_file}; then
        LOG_INFO "Download checksum from ${github_url}/${compatibility_version}/${release_pkg_checksum_file}"
        curl -LO ${github_url}/${compatibility_version}/${release_pkg_checksum_file}
    fi

    if  [ ! -e ${release_pkg_checksum_file} ] || [ -z "$(grep ${release_pkg} ${release_pkg_checksum_file})" ]; then
        LOG_ERROR "Download checksum file error"
        exit 1
    fi

    # download 
    if [ ! -f "${release_pkg}" ] || [ -z "$(md5sum -c ${release_pkg_checksum_file}|grep OK)" ];then

        LOG_INFO "Try to download from: ${cdn_url}/${compatibility_version}/${release_pkg}"
        if ! curl --fail -LO ${cdn_url}/${compatibility_version}/${release_pkg}; then
            # If CDN failed, download from github release
            LOG_INFO "Download from: ${github_url}/${compatibility_version}/${release_pkg}"
            curl -C - -LO ${github_url}/${compatibility_version}/${release_pkg}
        fi

        if [ -z "$(md5sum -c ${release_pkg_checksum_file}|grep OK)" ]; then
            LOG_ERROR "Download package error"
            rm -f ${release_pkg}
            exit 1
        fi

    else
        LOG_INFO "Latest release ${release_pkg} exists."
    fi

    tar -zxf ${release_pkg}
}

download_latest_code()
{
    local name=${1}
    local url=${2}
    local branch=${3}

    if [ -d ${name} ];then
        cd ${name}
        git checkout ${branch}
        git pull
        cd -
    else
        git clone --depth 1 -b ${branch} ${url}
    fi
}

build_from_source()
{
    LOG_INFO "Build WeCross Console from source"

    local url=${wecross_console_url}
    local branch=${wecross_console_branch}
    local output_dir=$(pwd)

    if [ -d WeCross-Console ];then
        LOG_INFO "./WeCross-Console/ exists"
        return
    fi

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross-Console ${url} ${branch}

    cd WeCross-Console
    rm -rf dist
    bash ./gradlew assemble 2>&1 | tee output.log
    chmod +x dist/apps/*
    # shellcheck disable=SC2046
    # shellcheck disable=SC2006
    if [ `grep -c "BUILD SUCCESSFUL" output.log` -eq '0' ]; then
        LOG_ERROR "Build Wecross Console project failed"
        LOG_INFO "See output.log for details"
        mv output.log ../output.log
        exit 1
    fi
    echo "================================================================"
    cd ..

    mv WeCross-Console/dist ${output_dir}/WeCross-Console

    cd ${output_dir}

    LOG_INFO "Build WeCross Console successfully"
}

main()
{
    if [ 1 -eq ${enable_build_from_resource} ];then
        build_from_source
    else
        download_wecross_console_pkg
    fi
}

print_result()
{
LOG_INFO "Download completed. WeCross Console is in: ./WeCross-Console/"
LOG_INFO "Please configure \"./WeCross-Console/conf/console.xml\" according with \"console-sample.xml\" and \"bash start.sh\" to start."
}

parse_command $@
main
print_result