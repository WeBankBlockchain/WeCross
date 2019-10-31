#!/bin/bash

set -e

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

function run_sed() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        sed -i "" "s/0xcdcce60801c0a2e6bb534322c32ae528b9dec8d2.pem/${pem_file}/g" 'src/test/resources/application.yml'
        sed -i "" "s/0x4c16922e30890eec4a3dfe9305cbdef9bdd57341.p12/${p12_file}/g" 'src/test/resources/application.yml'
    else
        # Other
        sed -i "s/0xcdcce60801c0a2e6bb534322c32ae528b9dec8d2.pem/${pem_file}/g" 'src/test/resources/application.yml'
        sed -i "s/0x4c16922e30890eec4a3dfe9305cbdef9bdd57341.p12/${p12_file}/g" 'src/test/resources/application.yml'
    fi
}

run_sed

mkdir -p src/test/resources/bcosconf/bcos1
mkdir -p src/test/resources/bcosconf/bcos2

cp accounts/* src/test/resources/bcosconf/bcos1
cp accounts/* src/test/resources/bcosconf/bcos2

rm -rf accounts

./gradlew verifyGoogleJavaFormat
./gradlew build -x test
./gradlew test -i
./gradlew jacocoTestReport
