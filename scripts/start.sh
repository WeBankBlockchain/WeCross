#!/bin/bash

rm -f start.out

wecross_pid=$(ps aux | grep com.webank.wecross.Application | grep -v grep | awk '{print $2}')
if [ ! -z ${wecross_pid} ]; then
    echo -e "\033[31m WeCross is running, pid is ${wecross_pid} \033[0m"
    exit 0
else
    nohup java -cp 'apps/*:lib/*:conf' com.webank.wecross.Application >start.out 2>&1 &
    sleep 10
fi

wecross_pid=$(ps aux | grep com.webank.wecross.Application | grep -v grep | awk '{print $2}')
failed_flag=$(tail -n20 start.out | grep error | grep -v asyncSendMessage)
if [[ -z ${wecross_pid} || ! -z "${failed_flag}" ]]; then
    echo -e "\033[31m WeCross start failed \033[0m"
    ps aux | grep com.webank.wecross.Application | grep -v grep | awk '{print $2}' | xargs kill -9
    exit 0
else
    echo -e "\033[32m WeCross start successfully \033[0m"
    exit 0
fi
sleep 0.5

echo -e "\033[31m Exceed waiting time. Please try again to start WeCross \033[0m"
tail -n20 start.out
exit 1
