#!/bin/bash

set -e

./gradlew assemble

mv dist demo/WeCross

cd demo

# demo test
bash build.sh <<EOF
    Y
    listResources
    listAccounts
    call payment.bcos.HelloWeCross bcos_user1 get
    sendTransaction payment.bcos.HelloWeCross bcos_user1 set Tom
    call payment.fabric.abac fabric_user1 query a
    sendTransaction payment.fabric.abac fabric_user1 invoke a b 10
    quit
EOF

# htlc test
bash htlc_config.sh


