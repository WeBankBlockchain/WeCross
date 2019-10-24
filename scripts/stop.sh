#!/bin/bash

rm -f start.out

wecross_pid=$(ps aux | grep com.webank.wecross.Application | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[31m WeCross isn't running \033[0m"
    exit 0
fi

ps aux | grep com.webank.wecross.Application | grep -v grep | awk '{print $2}' | xargs kill -9

wecross_pid=$(ps aux | grep com.webank.wecross.Application | grep -v grep | awk '{print $2}')
if [ -z ${wecross_pid} ]; then
    echo -e "\033[32m Stop WeCross successfully \033[0m"
    exit 0
fi
sleep 0.5

echo -e "\033[31m Exceed waiting time. Please try again to stop WeCross \033[0m"
exit 1
