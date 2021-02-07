#!/bin/bash
set -e

LANG=en_US.UTF-8

default_compatibility_version=v1.1.0 # update this every release

compatibility_version=
enable_build_from_resource=0

src_dir=$(pwd)'/src/'

wecross_account_manager_url=https://github.com/WebankBlockchain/WeCross-Account-Manager.git
wecross_account_manager_branch=${default_compatibility_version}

need_db_config_ask=true
DB_IP=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123456

LOG_INFO() {
    echo -e "\033[32m[INFO] $@\033[0m"
}

LOG_ERROR() {
    echo -e "\033[31m[ERROR] $@\033[0m"
}

help() {
    echo "$1"
    cat <<EOF
Usage:
    -s                              [Optional] Get wecross account manager by: gradle build from github Source Code.
    -b                              [Optional] Download from certain branch
    -t                              [Optional] Download from certain tag (same as -b)
    -d                              [Optional] Use default db configuration: -H ${DB_IP} -P ${DB_PORT} -u ${DB_USERNAME} -p ${DB_PASSWORD}
    -u                              [Optional] DB username
    -p                              [Optional] DB password
    -H                              [Optional] DB ip
    -P                              [Optional] DB port
    -h  call for help
e.g
    bash $0
    bash $0 -s
EOF
    exit 0
}

parse_command() {
    while getopts "u:p:H:P:b:t:sdh" option; do
        # shellcheck disable=SC2220
        case ${option} in
        s)
            enable_build_from_resource=1
            ;;
        b)
            wecross_account_manager_branch=$OPTARG
            compatibility_version=$OPTARG
            ;;
        t)
            wecross_account_manager_branch=$OPTARG
            compatibility_version=$OPTARG
            ;;
        d)
            need_db_config_ask=false
            ;;
        u)
            DB_USERNAME=$OPTARG
            need_db_config_ask=false
            ;;
        p)
            DB_PASSWORD=$OPTARG
            need_db_config_ask=false
            ;;
        H)
            DB_IP=$OPTARG
            need_db_config_ask=false
            ;;
        P)
            DB_PORT=$OPTARG
            need_db_config_ask=false
            ;;
        h) help ;;
        esac
    done

}

check_command() {
    local cmd=${1}
    if [ -z "$(command -v ${cmd})" ]; then
        LOG_ERROR "${cmd} is not installed."
        exit 1
    fi
}

query_db() {
    if [ ${DB_PASSWORD} ]; then
        mysql -u ${DB_USERNAME} --password="${DB_PASSWORD}" -h ${DB_IP} -P ${DB_PORT} $@ 2>/dev/null
    else
        mysql -u ${DB_USERNAME} -h ${DB_IP} -P ${DB_PORT} $@ 2>/dev/null
    fi
}

check_db_service() {
    LOG_INFO "Checking database configuration"
    set +e
    if ! query_db -e "status;" ; then
        LOG_ERROR "Database configuration error."
        LOG_INFO "Please config database, username and password. And use this command to check:"
        echo -e "\033[32m        mysql -u ${DB_USERNAME} --password=\"<your password>\" -h ${DB_IP} -P ${DB_PORT} -e \"status;\" \033[0m"
        exit 1
    fi
    set -e
    LOG_INFO "Database configuration OK!"
}

exit_when_empty_db_pwd() {
    if mysql -u ${DB_USERNAME} -h ${DB_IP} -P ${DB_PORT} -e "status" 2>/dev/null; then
        LOG_ERROR "Not support to use account with no password. Please try another account."
        exit 1
    fi
}

db_config_ask() {
    check_command mysql
    LOG_INFO "Database connection:"
    read -r -p "[1/4]> ip: " DB_IP
    read -r -p "[2/4]> port: " DB_PORT
    read -r -p "[3/4]> username: " DB_USERNAME
    exit_when_empty_db_pwd
    read -r -p "[4/4]> password: " -s DB_PASSWORD
    echo "" # \n
    LOG_INFO "Database connetion with: ${DB_IP}:${DB_PORT} ${DB_USERNAME} "
    check_db_service
}

config_database() {
    if ${need_db_config_ask}; then
        db_config_ask
    else
        check_db_service
    fi
}

download_wecross_account_manager_pkg() {
    local github_url=https://github.com/WebankBlockchain/WeCross-Account-Manager/releases/download/
    local cdn_url=https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeCross/WeCross-Account-Manager/
    local release_pkg=WeCross-Account-Manager.tar.gz
    local release_pkg_checksum_file=WeCross-Account-Manager.tar.gz.md5

    if [ -d WeCross-Account-Manager/apps ]; then
        LOG_INFO "./WeCross-Account-Manager/ exists"
        exit 0
    fi

    LOG_INFO "Checking latest release"
    if [ -z "${compatibility_version}" ]; then
        compatibility_version=$(curl -s https://api.github.com/repos/WebankBlockchain/WeCross-Account-Manager/releases/latest | grep "tag_name" | awk -F '\"' '{print $4}')
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

build_from_source() {
    LOG_INFO "Build WeCross Account Manager from source"

    local url=${wecross_account_manager_url}
    local branch=${wecross_account_manager_branch}
    local output_dir=$(pwd)

    if [ -d WeCross-Account-Manager ]; then
        LOG_INFO "./WeCross-Account-Manager/ exists"
        return
    fi

    mkdir -p ${src_dir}/
    cd ${src_dir}/

    download_latest_code WeCross-Account-Manager ${url} ${branch}

    cd WeCross-Account-Manager
    rm -rf dist
    bash ./gradlew assemble 2>&1 | tee output.log
    chmod +x dist/apps/*
    # shellcheck disable=SC2046
    # shellcheck disable=SC2006
    if [ $(grep -c "BUILD SUCCESSFUL" output.log) -eq '0' ]; then
        LOG_ERROR "Build Wecross Account Manager project failed"
        LOG_INFO "See output.log for details"
        mv output.log ../output.log
        exit 1
    fi
    echo "================================================================"
    cd ..

    mv WeCross-Account-Manager/dist ${output_dir}/WeCross-Account-Manager
    chmod +x ${output_dir}/WeCross-Account-Manager/*.sh

    cd ${output_dir}

    LOG_INFO "Build WeCross Account Manager successfully"
}

database_init() {
    query_db < ./WeCross-Account-Manager/conf/db_setup.sql
    LOG_INFO "Database initialize success:" $(cat ./WeCross-Account-Manager/conf/db_setup.sql)
}

main() {
    check_command mysql
    config_database
    if [ 1 -eq ${enable_build_from_resource} ]; then
        build_from_source
    else
        download_wecross_account_manager_pkg
    fi
    database_init
}

print_result() {
    LOG_INFO "Download completed. WeCross Account Manager is in: ./WeCross-Account-Manager/"
    LOG_INFO "Please configure \"./WeCross-Account-Manager/conf/application.toml\" according with \"application-sample.toml\" and \"bash start.sh\" to start."
}

parse_command $@
main
print_result
