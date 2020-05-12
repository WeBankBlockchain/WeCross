#!/bin/bash
set -e

LANG=en_US.utf8

enable_build_from_resource=0
compatibility_version=

default_compatibility_version=v1.0.0-rc2 # update this every release
deps_dir=$(pwd)'/WeCross/plugin/'
src_dir=$(pwd)'/src/'

wecross_url=https://github.com/WeBankFinTech/WeCross.git
wecross_branch=${default_compatibility_version}

bcos_stub_url=https://github.com/WeBankFinTech/WeCross-BCOS2-Stub.git
bcos_stub_branch=${default_compatibility_version}

fabric_stub_url=https://github.com/WeBankFinTech/WeCross-Fabric1-Stub.git
fabric_stub_branch=${default_compatibility_version}


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
    -b                              [Optional] Download from certain branch
    -t                              [Optional] Download from certain tag (same as -b)
    -h  call for help
e.g
    bash $0 
    bash $0 -s 
EOF
exit 0
}


parse_command()
{
while getopts "b:t:sh" option;do
    # shellcheck disable=SC2220
    case ${option} in
    s)
        enable_build_from_resource=1
    ;;
    b)
        wecross_branch=$OPTARG
        bcos_stub_branch=$OPTARG
        fabric_stub_branch=$OPTARG
        compatibility_version=$OPTARG
    ;;
    t)
        wecross_branch=$OPTARG
        bcos_stub_branch=$OPTARG
        fabric_stub_branch=$OPTARG
        compatibility_version=$OPTARG
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
    if [ ! -f "${release_pkg}" ] || [ "$(md5sum -c ${release_pkg_checksum_file}|echo $?)" -ne "0" ];then

        LOG_INFO "Try to download from: ${cdn_url}/${compatibility_version}/${release_pkg}"
        if ! curl --fail -LO ${cdn_url}/${compatibility_version}/${release_pkg}; then
            # If CDN failed, download from github release
            LOG_INFO "Download from: ${github_url}/${compatibility_version}/${release_pkg}"
            curl -C - -LO ${github_url}/${compatibility_version}/${release_pkg}
        fi

        if [ "$(md5sum -c ${release_pkg_checksum_file}|echo $?)" -ne "0" ]; then
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
    LOG_INFO "Build WeCross from source"

    local url=${wecross_url}
    local branch=${wecross_branch}
    local output_dir=$(pwd)

    if [ -d WeCross ];then
        LOG_INFO "./WeCross/ exists"
        return
    fi

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross ${url} ${branch}

    cd WeCross
    rm -rf dist
    bash ./gradlew assemble 2>&1 | tee output.log
    chmod +x dist/apps/*
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

    mv WeCross/dist ${output_dir}/WeCross

    chmod +x ${output_dir}/WeCross/*.sh

    cd ${output_dir}
}

build_plugin_from_source()
{
    local name=${1}
    local url=${2}
    local branch=${3}
    local origin_dir=$(pwd)

    LOG_INFO "Build ${name} from source"

    if [ -d ${name} ];then
        LOG_INFO "./${name}/ exists"
        return
    fi

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
}

main()
{
    if [ 1 -eq ${enable_build_from_resource} ];then
        build_from_source
        build_plugin_from_source WeCross-BCOS2-Stub ${bcos_stub_url} ${bcos_stub_branch}
        build_plugin_from_source WeCross-Fabric1-Stub ${fabric_stub_url} ${fabric_stub_branch}
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
