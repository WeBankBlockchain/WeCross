#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}

wecross_account_manager_deploy_dir=''
wecross_account_manager_upgrade_dir=${dirpath}/WeCross-Account-Manager

LOG_WARN() {
    local content=${1}
    echo -e "\033[31m[ERROR] $@\033[0m"
}

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] $@\033[0m"
}

LOG_FALT() {
    local content=${1}
    echo -e "\033[31m[FALT] $@\033[0m"
    exit 1
}

file_must_exists() {
    if [ ! -f "$1" ]; then
        LOG_FALT "$1 file does not exist, please check!"
    fi
}

file_must_not_exists() {
    if [ -f "$1" ]; then
        LOG_FALT "$1 file exists, please check!"
    fi
}

dir_must_exists() {
    if [ ! -d "$1" ]; then
        LOG_FALT "$1 DIR does not exist, please check!"
    fi
}

dir_must_not_exists() {
    if [ -e "$1" ]; then
        LOG_FALT "$1 DIR exists, please clean old DIR!"
    fi
}

help() {
    echo $1
    cat <<EOF
Usage: 
    -s <upgrade dir>                    [Optional] the WeCross Account Manager's upgrade dir
    -d <account manager's dir>          [Optional] the WeCross Account Manager's deploy dir
    -h                                  [Optional] Help
e.g 
    bash $0 -d ~/wecross-demo/WeCross-Account-Manager
EOF

    exit 0
}

while getopts "s:d:h" option; do
    case $option in
    s) wecross_account_manager_upgrade_dir=$OPTARG ;;
    d) wecross_account_manager_deploy_dir=$OPTARG ;;
    *) help ;;
    esac
done

main() {
    dir_must_exists "${wecross_account_manager_deploy_dir}"
    dir_must_exists "${wecross_account_manager_deploy_dir}"/apps
    dir_must_exists "${wecross_account_manager_deploy_dir}"/lib

    dir_must_exists "${wecross_account_manager_upgrade_dir}"
    dir_must_exists "${wecross_account_manager_upgrade_dir}"/apps
    dir_must_exists "${wecross_account_manager_upgrade_dir}"/lib

    # clean old deps
    rm -rf "${wecross_account_manager_deploy_dir}"/apps/*jar
    rm -rf "${wecross_account_manager_deploy_dir}"/lib/*jar

    # copy upgraded deps
    cp "${wecross_account_manager_upgrade_dir}"/apps/*jar "${wecross_account_manager_deploy_dir}"/apps/
    cp "${wecross_account_manager_upgrade_dir}"/lib/*jar "${wecross_account_manager_deploy_dir}"/lib/
    cp "${wecross_account_manager_upgrade_dir}"/start.sh "${wecross_account_manager_deploy_dir}"
    cp "${wecross_account_manager_upgrade_dir}"/stop.sh "${wecross_account_manager_deploy_dir}"

    LOG_INFO " WeCross-Account-Manager upgrade completed:"
    LOG_INFO " \t version: "$(ls ${wecross_account_manager_deploy_dir}/apps/ |awk '{gsub(/.jar$/,""); print}')
}

main
