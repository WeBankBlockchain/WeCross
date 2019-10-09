#!/bin/bash

set -e

curl -LO https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/dev/tools/build_chain.sh && chmod u+x build_chain.sh
bash <(curl -s https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/dev/tools/ci/download_bin.sh) -b dev -m
echo "127.0.0.1:4 agency1 1,2,3" > ipconf
./build_chain.sh -e bin/fisco-bcos -f ipconf -p 30300,20200,8545 -v 2.1.0
./nodes/127.0.0.1/start_all.sh
./nodes/127.0.0.1/fisco-bcos -v
cp nodes/127.0.0.1/sdk/* src/integration-test/resources/
mv src/integration-test/resources/applicationContext-sample.xml src/integration-test/resources/applicationContext.xml
./gradlew verifyGoogleJavaFormat
./gradlew build
./gradlew test
# ./gradlew integrationTest