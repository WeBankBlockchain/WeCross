#!/bin/bash

function run_wecross() {
    java -cp 'apps/*:lib/*:conf' com.webank.wecross.Application
}

function run_script() {
    run_wecross
}

run_script
