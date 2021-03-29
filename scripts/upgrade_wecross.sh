#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}

router_deploy_dir=''
router_upgrade_dir=${dirpath}/WeCross

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
    -s <upgrade dir>                    [Optional] the router's upgrade dir
    -d <router dir>                     [Optional] the router's deploy dir
    -h                                  [Optional] Help
e.g 
    bash $0 -d ~/wecross-demo/router/127.0.0.1-8250-25500/ 
EOF

    exit 0
}

while getopts "s:d:h" option; do
    case $option in
    s) router_upgrade_dir=$OPTARG ;;
    d) router_deploy_dir=$OPTARG ;;
    *) help ;;
    esac
done

main() {
    dir_must_exists "${router_deploy_dir}"
    dir_must_exists "${router_deploy_dir}"/apps
    dir_must_exists "${router_deploy_dir}"/lib
    dir_must_exists "${router_deploy_dir}"/plugin
    dir_must_exists "${router_deploy_dir}"/pages

    dir_must_exists "${router_upgrade_dir}"
    dir_must_exists "${router_upgrade_dir}"/apps
    dir_must_exists "${router_upgrade_dir}"/lib
    dir_must_exists "${router_upgrade_dir}"/plugin
    dir_must_exists "${router_upgrade_dir}"/pages

    # clean old deps
    rm -rf "${router_deploy_dir}"/apps/*jar
    rm -rf "${router_deploy_dir}"/lib/*jar
    rm -rf "${router_deploy_dir}"/plugin/*jar
    rm -rf "${router_deploy_dir}"/plugin/*jar
    rm -rf "${router_deploy_dir}"/pages

    # copy upgraded deps
    cp "${router_upgrade_dir}"/apps/*jar "${router_deploy_dir}"/apps/
    cp "${router_upgrade_dir}"/lib/*jar "${router_deploy_dir}"/lib/
    cp "${router_upgrade_dir}"/plugin/*jar "${router_deploy_dir}"/plugin/
    cp "${router_upgrade_dir}"/start.sh "${router_deploy_dir}"/
    cp "${router_upgrade_dir}"/stop.sh "${router_deploy_dir}"/
    cp -r "${router_upgrade_dir}"/pages "${router_deploy_dir}"/

    LOG_INFO " Router upgrade completed:"
    LOG_INFO " \t router version: "$(ls ${router_deploy_dir}/apps/ |awk '{gsub(/.jar$/,""); print}')
    LOG_INFO " \t plugin version: "$(ls ${router_deploy_dir}/plugin/ |awk '{gsub(/.jar$/,""); print}')
}

main
