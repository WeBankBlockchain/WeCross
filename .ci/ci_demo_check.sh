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
call payment.fabric.abac fabric_user1 query a
call payment.fabric.abac fabric_user1 query b
quit
EOF
cd ..

# htlc test
bash htlc_config.sh

cd WeCross-Console/
bash start.sh <<EOF
call payment.bcos.htlc bcos_sender balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
newHTLCTransferProposal payment.bcos.htlc bcos_sender bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 9dda9a5e175a919ee98ff0198927b0a765ef96cf917144b589bb8e510e04843c true 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org1.example.com 500 2000000000
quit
EOF
cd ..

cd WeCross-Console-8251/
bash start.sh <<EOF
call payment.fabric.htlc fabric_admin balanceOf User1@org1.example.com
newHTLCTransferProposal payment.fabric.htlc fabric_admin bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 null false 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org1.example.com 500 2000000000
quit
EOF
cd ..

sleep 30

cd WeCross-Console/
bash start.sh <<EOF
call payment.bcos.htlc bcos_sender balanceOf 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf
quit
EOF
cd ..

cd WeCross-Console-8251/
bash start.sh <<EOF
call payment.fabric.htlc fabric_admin balanceOf User1@org1.example.com
quit
EOF
cd ..


# check error
error_log=routers-payment/127.0.0.1-8250-25500/logs/error.log
if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
    cat ${error_log}
    echo "Error log is ${error_log}"
    exit 1
fi

error_log=routers-payment/127.0.0.1-8251-25501/logs/error.log
if [ "$(grep ERROR ${error_log} |wc -l)" -ne "0" ];then
    cat ${error_log}
    echo "Error log is ${error_log}"
    exit 1
fi


