#!/bin/bash

rm -f start.out

APPS_FOLDER=$(pwd)/apps
CLASS_PATH=$(pwd)'/apps/*:lib/*:conf'
WINDS_CLASS_PATH=$(pwd)'/apps/*;lib/*;conf'

run_wecross() 
{
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        nohup java -Djdk.tls.namedGroups="secp256k1" -cp ${CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    elif [ "$(uname -s | grep MINGW | wc -l)" != "0" ]; then
        # Windows
        nohup java -Djdk.tls.namedGroups="secp256k1" -cp ${WINDS_CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    else
        # GNU/Linux
        nohup java -Djdk.tls.namedGroups="secp256k1" -cp ${CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    fi
}

waiting_for_start()
{
    echo -e "\033[32mWeCross booting up ..\033[0m\c"
    try_times=20
    i=0
    while [ $i -lt ${try_times} ]
    do
        sleep 1
        [ ! -z "$(grep PeerManager start.out)" ] && break
        echo -e "\033[32m.\033[0m\c"
        ((i=i+1))
    done
    echo ""
}

wecross_pid=$(ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ ! -z ${wecross_pid} ]; then
    echo -e "\033[31mWeCross is running, pid is ${wecross_pid} \033[0m"
    exit 0
else
    run_wecross
fi

waiting_for_start

wecross_pid=$(ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[31mWeCross start failed \033[0m"
    echo -e "\033[31mSee logs/error.log for details \033[0m"
    exit 0
else
    echo -e "\033[32mWeCross start successfully \033[0m"
    exit 0
fi
sleep 0.5

echo -e "\033[31mExceed waiting time. Please try again to start WeCross \033[0m"
tail -n20 start.out
exit 1
