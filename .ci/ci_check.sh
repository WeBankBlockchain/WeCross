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

#configure WeCross
sed -i "s/0xb5d83b5265756ec114f13226efd341342d9ed49f/${hello_address}/" src/main/resources/application-sample.yml

#configure WeCross test
mkdir -p src/test/resources/bcosconf/bcos1/
mkdir -p src/test/resources/bcosconf/bcos2/
cp nodes/127.0.0.1/sdk/* src/test/resources/bcosconf/bcos1/
cp nodes/127.0.0.1/sdk/* src/test/resources/bcosconf/bcos2/
cp src/main/resources/application-sample.yml src/test/resources/application.yml

./gradlew verifyGoogleJavaFormat
./gradlew build -x test

#start
mkdir -p dist/conf/bcosconf/bcos1/
mkdir -p dist/conf/bcosconf/bcos2/
cp nodes/127.0.0.1/sdk/* dist/conf/bcosconf/bcos1/
cp nodes/127.0.0.1/sdk/* dist/conf/bcosconf/bcos2/
cp src/main/resources/application-sample.yml dist/conf/application.yml
cd dist
bash start.sh &
echo "waiting for startup"
sleep 10
cd ..

./gradlew test
./gradlew jacocoTestReport
