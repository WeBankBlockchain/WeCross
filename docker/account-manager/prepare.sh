#!/bin/bash

# set -e
# LANG=en_US.UTF-8

ROOT=$(pwd)

cd ${ROOT}
mkdir -p temp && cd temp

bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_account_manager.sh) -d -s -b $1

cd ${ROOT}
mkdir -p wecross-account-manager
cp -r temp/WeCross-Account-Manager/* wecross-account-manager/


cat >>${ROOT}/wecross-account-manager/start.sh <<EOF
while true; do
  sleep 1
  ttime=`date +"%Y-%m-%d %H:%M:%S"`
  echo $ttime
done
EOF

tar -zcvf wecross-account-manager.tar.gz wecross-account-manager

mkdir -p account-conf
cp -r temp/WeCross-Account-Manager/conf/* account-conf/
cp -r temp/WeCross-Account-Manager/create_rsa_keypair.sh account-conf/

rm -rf temp
rm -rf wecross-account-manager
