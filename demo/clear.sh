#!/bin/bash
ROOT=$(pwd)
cd ${ROOT}/routers-payment/127.0.0.1-8250-25500/
bash ./stop.sh
cd ${ROOT}/routers-payment/127.0.0.1-8251-25501/
bash stop.sh

cd ${ROOT}
rm -rf WeCross WeCross-Console WeCross-Console-8251 ipfile routers-payment

cd ${ROOT}/bcos
bash clear.sh

cd ${ROOT}/fabric
bash clear.sh