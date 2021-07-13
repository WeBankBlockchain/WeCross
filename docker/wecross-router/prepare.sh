#!/bin/bash

# set -e
# LANG=en_US.UTF-8

ROOT=$(pwd)

cd ${ROOT}
mkdir -p temp && cd temp

bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_wecross.sh) -d -s -b $1

bash ./WeCross/build_wecross.sh -n payment -l 127.0.0.1:8250:25500

cp -r routers/cert routers/127.0.0.1-8250-25500/

cd ${ROOT}
mkdir -p wecross-router
cp -r temp/routers/127.0.0.1-8250-25500/* wecross-router/

cat >>${ROOT}/wecross-router/start.sh <<EOF
while true; do
  sleep 1
  ttime=`date +"%Y-%m-%d %H:%M:%S"`
  echo $ttime
done
EOF

tar -zcvf wecross-router.tar.gz wecross-router

mkdir -p router-conf
cp -r temp/routers/127.0.0.1-8250-25500/conf/* router-conf/
cp -r temp/routers/127.0.0.1-8250-25500/cert/create_cert.sh router-conf/

rm -rf temp
rm -rf wecross-router