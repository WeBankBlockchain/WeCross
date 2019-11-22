#!/bin/bash

rm -f start.out

APPS_FOLDER=$(pwd)/apps

wecross_pid=$(ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[31m Wecross isn't running \033[0m"
    exit 0
fi

ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}' | xargs kill -9

wecross_pid=$(ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[32m Stop Wecross successfully \033[0m"
    exit 0
fi
sleep 0.5

echo -e "\033[31m Exceed waiting time. Please try again to stop WeCross \033[0m"
exit 1
