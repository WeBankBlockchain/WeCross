#!/bin/bash
set -e

LANG=en_US.utf8

enable_build_from_resource=0


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
    -s                              [Optional] Get wecross by: gradle build from github Source Code.
    -h  call for help
e.g
    bash $0 
    bash $0 -s 
EOF
exit 0
}


parse_command()
{
while getopts "sh" option;do
    # shellcheck disable=SC2220
    case ${option} in
    s)
        enable_build_from_resource=1
    ;;
    h)  help;;
    esac
done

}

parallel_download()
{
    #parallel_download WeCross.tar.bz2.md5 https://github.com/WeBankFinTech/WeCross/releases/download/${compatibility_version}
    md5_file_list=${1}
    prefix=${2}
    # shellcheck disable=SC2162
    
    while read line;do
        local part=$(echo ${line}|awk '{print $2}')
        curl -s -C - -LO ${prefix}/${part} &
    done < "${md5_file_list}"   
    wait
    
    if [ ! -z "$(md5sum -c ${md5_file_list}|grep FAILED)" ];then
        LOG_ERROR "Download WeCross package failed! URL: ${prefix}"
        exit 1
    fi
}

download_wecross_pkg()
{
    local github_url=https://github.com/WeBankFinTech/WeCross/releases/download/
    local cdn_url=https://www.fisco.com.cn/cdn/wecross/releases/download/
    local compatibility_version=
    local release_pkg=WeCross.tar.gz
    local release_pkg_checksum_file=WeCross.tar.gz.md5

    if [ -d WeCross/apps ];then
        LOG_INFO "./WeCross/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ];then
        compatibility_version=$(curl -s https://api.github.com/repos/WeBankFinTech/WeCross/releases/latest | grep "tag_name"|awk -F '\"' '{print $4}')
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

build_from_source()
{
    if [ -d WeCross/apps ];then
        LOG_INFO "./WeCross/ exists"
        exit 0
        return
    fi

    git clone https://github.com/WeBankFinTech/WeCross.git
    cd WeCross
    ./gradlew assemble 2>&1 | tee output.log
    # shellcheck disable=SC2046
    # shellcheck disable=SC2006
    if [ `grep -c "BUILD SUCCESSFUL" output.log` -eq '0' ]; then
        LOG_ERROR "Build Wecross project failed"
        LOG_INFO "See output.log for details"
        mv output.log ../output.log
        cd ..
        exit 1
    fi
    cd ..
    mv WeCross WeCross-Source
    mv WeCross-Source/dist WeCross
    rm -rf WeCross-Source
}

main()
{
    if [ 1 -eq ${enable_build_from_resource} ];then
        build_from_source
    else
        download_wecross_pkg
    fi
}

print_result()
{
LOG_INFO "Download completed. WeCross is in: ./WeCross/"
}

parse_command $@
main
print_result
