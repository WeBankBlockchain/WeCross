#!/bin/bash
ROOT=$(pwd)
router8250=${ROOT}/routers-payment/127.0.0.1-8250-25500/
router8251=${ROOT}/routers-payment/127.0.0.1-8251-25501/
account_manager=${ROOT}/WeCross-Account-Manager

if [ -d ${router8250} ]; then
    cd ${router8250}
    bash ./stop.sh
fi

if [ -d ${router8251} ]; then
    cd ${router8251}
    bash ./stop.sh
fi

if [ -d ${account_manager} ]; then
    cd ${account_manager}
    bash ./stop.sh
fi

cd ${ROOT}
rm -f WeCross-Account-Manager/conf/rsa_p*
rm -rf WeCross WeCross-Console WeCross-Console-8251 WeCross-Account-Manager ipfile routers-payment

cd ${ROOT}/bcos
bash clear.sh

cd ${ROOT}/bcos3
bash clear.sh

cd ${ROOT}/fabric
bash clear.sh

cd ${ROOT}/fabric2
bash clear.sh
