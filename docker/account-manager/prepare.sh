#!/bin/bash

# set -e
# LANG=en_US.UTF-8

ROOT=$(pwd)
wecross_account_manager_branch=dev
DB_IP=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123456

parse_command() {
    while getopts "u:p:H:P:b:" option; do
        # shellcheck disable=SC2220
        case ${option} in
        u)
            DB_USERNAME=$OPTARG
            ;;
        p)
            DB_PASSWORD=$OPTARG
            ;;
        H)
            DB_IP=$OPTARG
            ;;
        P)
            DB_PORT=$OPTARG
            ;;
        b)
            wecross_account_manager_branch=$OPTARG
            ;;
        esac
    done

}

parse_command $@

cd ${ROOT}
mkdir -p temp && cd temp

bash <(curl -sL https://gitee.com/WeBank/WeCross/raw/master/scripts/download_account_manager.sh) -H ${DB_IP} -P ${DB_PORT} -u ${DB_USERNAME} -p ${DB_PASSWORD} -s -b ${wecross_account_manager_branch}

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
