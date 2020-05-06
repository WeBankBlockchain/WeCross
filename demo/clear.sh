#!/bin/bash
ROOT=$(pwd)
router8250=${ROOT}/routers-payment/127.0.0.1-8250-25500/
router8251=${ROOT}/routers-payment/127.0.0.1-8251-25501/

if [ -d ${router8250} ]; then
    cd ${router8250}
    bash ./stop.sh
fi

if [ -d ${router8251} ]; then
    cd ${router8251}
    bash ./stop.sh
fi

cd ${ROOT}
rm -rf WeCross WeCross-Console WeCross-Console-8251 ipfile routers-payment

cd ${ROOT}/bcos
bash clear.sh

cd ${ROOT}/fabric
bash clear.sh