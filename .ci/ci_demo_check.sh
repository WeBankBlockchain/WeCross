#!/bin/bash

set -e

./gradlew assemble

mv dist demo/WeCross

cd demo

# demo test
bash build.sh n

cd WeCross-Console/
bash start.sh <<EOF
listResources
listAccounts
call payment.bcos.HelloWeCross bcos_user1 get
sendTransaction payment.bcos.HelloWeCross bcos_user1 set Tom
call payment.fabric.abac fabric_user1 query a
sendTransaction payment.fabric.abac fabric_user1 invoke a b 10
quit
EOF
cd ..

# htlc test
bash htlc_config.sh

# check error
error_log=routers-payment/127.0.0.1-8250-25500/logs/error.log
if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
    cat ${error_log}
    echo ${error_log}
    exit 1
fi

error_log=routers-payment/127.0.0.1-8251-25501/logs/error.log
if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
    cat ${error_log}
    echo ${error_log}
    exit 1
fi


