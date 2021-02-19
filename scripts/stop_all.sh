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
    if [[ -d "${SHELL_FOLDER}/${directory}" && -f "${SHELL_FOLDER}/${directory}/stop.sh" ]];then
        echo "Try to stop router: ${directory}"
        bash ${SHELL_FOLDER}/${directory}/stop.sh
    fi
done
wait
