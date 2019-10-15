#!/bin/bash

function run_wecross() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        java -cp 'apps/*:lib/*:conf' com.webank.wecross.Application
    elif [ "$(uname -s | grep MINGW | wc -l)" != "0" ]; then
        # Windows
        java -cp 'apps/*;lib/*;conf' com.webank.wecross.Application
    else
        # GNU/Linux
        java -cp 'apps/*:lib/*:conf' com.webank.wecross.Application
    fi
}

function run_script() {
    run_wecross
}

run_script
