#!/bin/bash

rm -f start.out

APPS_FOLDER=$(pwd)/apps
CLASS_PATH=$(pwd)'/apps/*:lib/*:conf'

function run_wecross() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        nohup java -cp ${CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    elif [ "$(uname -s | grep MINGW | wc -l)" != "0" ]; then
        # Windows
        nohup java -cp 'apps/*;lib/*;conf' com.webank.wecross.Service >start.out 2>&1 &
    else
        # GNU/Linux
        nohup java -cp 'apps/*:lib/*:conf' com.webank.wecross.Service >start.out 2>&1 &
    fi
}

wecross_pid=$(ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ ! -z ${wecross_pid} ]; then
    echo -e "\033[31m Wecross is running, pid is ${wecross_pid} \033[0m"
    exit 0
else
    run_wecross
    sleep 10
fi

wecross_pid=$(ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[31m Wecross start failed \033[0m"
    echo -e "\033[31m See logs/error.log for details \033[0m"
    exit 0
else
    echo -e "\033[32m Wecross start successfully \033[0m"
    exit 0
fi
sleep 0.5

echo -e "\033[31m Exceed waiting time. Please try again to start WeCross \033[0m"
tail -n20 start.out
exit 1
