#!/bin/bash

set -e

curl -LO https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/dev/tools/build_chain.sh && chmod u+x build_chain.sh
bash <(curl -s https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/dev/tools/ci/download_bin.sh) -b dev
echo "127.0.0.1:4 agency1 1,2,3" >ipconf
./build_chain.sh -e bin/fisco-bcos -f ipconf -p 30300,20200,8545 -v 2.1.0
./nodes/127.0.0.1/start_all.sh
./nodes/127.0.0.1/fisco-bcos -v

#config console
bash <(curl -s https://raw.githubusercontent.com/FISCO-BCOS/console/master/tools/download_console.sh)
cp -n console/conf/applicationContext-sample.xml console/conf/applicationContext.xml
cp nodes/127.0.0.1/sdk/* console/conf/

#deploy helloworld
cd console
bash start.sh <<EOF
deploy HelloWorld
EOF
hello_address=$(grep 'HelloWorld' deploylog.txt | awk '{print $5}')
cd -

#generate crt file
expect <<EOF
spawn bash ./scripts/get_bcos_account.sh
expect "Password"
send "123456\r"
expect "Password"
send "123456\r"
expect eof
EOF

rm accounts/*.public.*
cd accounts
pem_file=$(ls *.pem | awk -F'.' '{print $0}')
p12_file=$(ls *.p12 | awk -F'.' '{print $0}')
cd -

cp src/test/resources/application-sample.yml src/test/resources/application.yml

#generate wecross cert
bash ./scripts/build_cert.sh -c -d ./ca
bash ./scripts/build_cert.sh -n -D ./ca -d ./ca/node
mkdir ./test/resources/p2p
cp ./ca/ca.crt ./test/resources/p2p/
cp ./ca/node/node.crt ./test/resources/p2p/
cp ./ca/node/node.key ./test/resources/p2p/
cp ./ca/node/node.nodeid ./test/resources/p2p/

#configure wecross
if [ "$(uname)" == "Darwin" ]; then
    # Mac
    sed -i "" "s/0x38735ad749aebd9d6e9c7350ae00c28c8903dc7a/${hello_address}/g" src/test/resources/application.yml
    sed -i "" "s/0xcdcce60801c0a2e6bb534322c32ae528b9dec8d2.pem/${pem_file}/g" src/test/resources/application.yml
    sed -i "" "s/0x4c16922e30890eec4a3dfe9305cbdef9bdd57341.p12/${p12_file}/g" src/test/resources/application.yml
else
    # Other
    sed -i "s/0x38735ad749aebd9d6e9c7350ae00c28c8903dc7a/${hello_address}/g" src/test/resources/application.yml
    sed -i "s/0xcdcce60801c0a2e6bb534322c32ae528b9dec8d2.pem/${pem_file}/g" src/test/resources/application.yml
    sed -i "s/0x4c16922e30890eec4a3dfe9305cbdef9bdd57341.p12/${p12_file}/g" src/test/resources/application.yml
fi

mkdir -p src/test/resources/bcosconf/bcos1
mkdir -p src/test/resources/bcosconf/bcos2

cp accounts/* src/test/resources/bcosconf/bcos1
cp accounts/* src/test/resources/bcosconf/bcos2

cp nodes/127.0.0.1/sdk/* src/test/resources/bcosconf/bcos1/
cp nodes/127.0.0.1/sdk/* src/test/resources/bcosconf/bcos2/

rm -rf accounts

./gradlew verifyGoogleJavaFormat
./gradlew build -x test
./gradlew test -i
./gradlew jacocoTestReport
