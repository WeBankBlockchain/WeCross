#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)

LOG_ERROR() {
    echo -e "\033[31m[ERROR] $@\033[0m"
}

LOG_INFO() {
    echo -e "\033[32m[INFO] $@\033[0m"
}

dirs=($(ls -l ${SHELL_FOLDER} | awk '/^d/ {print $NF}'))
for directory in ${dirs[*]}
do
    if [[ -f "${SHELL_FOLDER}/${directory}/conf/wecross.toml" && -f "${SHELL_FOLDER}/${directory}/start.sh" ]];then
        echo "Try to start router: ${directory}"
        bash ${SHELL_FOLDER}/${directory}/start.sh
    fi
done
wait
