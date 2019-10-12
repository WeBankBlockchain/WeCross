#!/bin/bash

function run_wecross() {
    if [ "$(uname -s|grep MINGW |wc -l)" != "0" ];then
        #win
        java -cp 'apps/*;lib/*;conf' com.webank.wecross.Application
    else
        #linux
        java -cp 'apps/*:lib/*:conf' com.webank.wecross.Application
    fi
}

function run_script() {
    run_wecross
}

run_script
