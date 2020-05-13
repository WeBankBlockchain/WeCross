#!/bin/bash

APPS_FOLDER=$(pwd)/apps
CLASS_PATH=$(pwd)'/apps/*:lib/*:conf:plugin/*'
WINDS_CLASS_PATH=$(pwd)'/apps/*;lib/*;conf;plugin/*'

STATUS_STARTING="Starting"
STATUS_RUNNING="Running"
STATUS_STOPPED="Stopped"

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m${content}\033[0m"
}

wecross_pid()
{
    ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}'
}

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

wecross_status()
{
    if [ ! -z $(wecross_pid) ]; then
        if [ ! -z "$(grep "WeCross router start success" start.out)" ]; then
            echo ${STATUS_RUNNING}
        else
            echo ${STATUS_STARTING}
        fi
    else
        echo ${STATUS_STOPPED}
    fi
}

tail_log()
{
    tail -n 30 logs/error.log
    tail -n 30 start.out
}

before_start()
{
    local status=$(wecross_status)

    case ${status} in
        ${STATUS_STARTING})
            LOG_ERROR "WeCross is starting, pid is $(wecross_pid)"
            exit 0
            ;;
        ${STATUS_RUNNING})
            LOG_ERROR "WeCross is running, pid is $(wecross_pid)"
            exit 0
            ;;
        ${STATUS_STOPPED})
            # do nothing
            ;;
        *)
            exit 1
            ;;
    esac
}

start()
{
    rm -f start.out
    run_wecross
    echo -e "\033[32mWeCross booting up ..\033[0m\c"
    try_times=30
    i=0
    while [ $i -lt ${try_times} ]
    do
        sleep 1
        local status=$(wecross_status)

        case ${status} in
            ${STATUS_STARTING})
                echo -e "\033[32m.\033[0m\c"
                ;;
            ${STATUS_RUNNING})
                break
                ;;
            ${STATUS_STOPPED})
                break
                ;;
            *)
                exit 1
                ;;
        esac

        ((i=i+1))
    done
    echo ""
}

after_start()
{
    local status=$(wecross_status)

    case ${status} in
        ${STATUS_STARTING})
            kill $(wecross_pid)
            LOG_ERROR "Exceed waiting time. Killed. Please try to start WeCross again"
            tail_log
            exit 1
            ;;
        ${STATUS_RUNNING})
            LOG_INFO "WeCross start successfully!"
            ;;
        ${STATUS_STOPPED})
            LOG_ERROR "WeCross start failed"
            LOG_ERROR "See logs/error.log for details"
            tail_log
            exit 1
            ;;
        *)
            exit 1
            ;;
    esac
}

main()
{
    before_start
    start
    after_start
}

main

