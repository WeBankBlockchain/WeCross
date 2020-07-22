#!/bin/bash
set -e
LANG=en_US.utf8
ROOT=$(pwd)

version=$RANDOM
BCOS_LEDGER=
BCOS_HTLC=
FABRIC_LEDGER=ledger
FABRIC_HTLC=htlc
DOCKER_ID=

## address = 0x55f934bcbe1e9aef8337f5551142a442fdde781c
BCOS_SENDER=$(echo "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tDQpNSUdFQWdFQU1CQUdCeXFHU000OUFnRUdCU3VCQkFBS0JHMHdhd0lCQVFRZzk3b2dnSysxdVpHZFpiWkdXSUhDDQpwR0gwSVU3Q2ZQUWZXRDNlM0lsZWR4R2hSQU5DQUFRbUhLU3d4bVZKRWRubWRKVVlHOUlLeitjYVE5blRiT2tmDQpDaW1JcmR4ZmhZMjF2c2tybUZVck5sYU5yRE1INE9wQysrZHRsbHE5UXh2dmFUbEltMjJ4DQotLS0tLUVORCBQUklWQVRFIEtFWS0tLS0t" | base64 -d)

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

config_bcos_sender_account()
{
    LOG_INFO "Config account for BCOS htlc sender ..."
    mkdir routers-payment/127.0.0.1-8250-25500/conf/accounts/bcos_sender
    cat >>account.key<<EOF
${BCOS_SENDER}
EOF
    mv account.key routers-payment/127.0.0.1-8250-25500/conf/accounts/bcos_sender
    cat << EOF > routers-payment/127.0.0.1-8250-25500/conf/accounts/bcos_sender/account.toml
[account]
type = 'BCOS2.0'
accountFile = 'account.key'
EOF
}

init_bcos_htlc()
{
    LOG_INFO "Init BCOS htlc ..."

    cd WeCross-Console
    bash start.sh <<EOF
bcosDeploy payment.bcos.ledger bcos_sender conf/contracts/solidity/LedgerSample.sol LedgerSample ${version} htlc token 1 100000000
quit
EOF
  var1=$(cat logs/info.log | grep data=0x | awk 'END{print $11}')
  BCOS_LEDGER=$(echo ${var1:5:42})

    bash start.sh <<EOF
bcosDeploy payment.bcos.htlc bcos_sender conf/contracts/solidity/LedgerSampleHTLC.sol LedgerSampleHTLC ${version}
quit
EOF
  var2=$(cat logs/info.log | grep data=0x | awk 'END{print $11}')
  BCOS_HTLC=$(echo ${var2:5:42})

  bash start.sh <<EOF
sendTransaction payment.bcos.ledger bcos_sender approve ${BCOS_HTLC} 1000000
sendTransaction payment.bcos.htlc bcos_sender init ${BCOS_LEDGER} ${FABRIC_HTLC}
quit
EOF
    cd -
}

copy_fabric_chaincode()
{
    LOG_INFO "Copy Fabric chaincode ..."

    docker ps -a|grep cli > tmp.txt
    DOCKER_ID=$(grep 'fabric' tmp.txt | awk '{print $1}')

    docker cp WeCross-Console/conf/contracts/chaincode/ledger ${DOCKER_ID}:/opt/gopath/src/github.com/chaincode/ledger
    docker cp WeCross-Console/conf/contracts/chaincode/htlc ${DOCKER_ID}:/opt/gopath/src/github.com/chaincode/htlc
}

install_fabric_chaincode()
{
    LOG_INFO "Install Fabric chaincode ..."

    # install ledger
    docker exec -it cli peer chaincode install -n ${FABRIC_LEDGER} -v ${version} -p github.com/chaincode/ledger/
    docker exec -it cli peer chaincode instantiate -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n ${FABRIC_LEDGER} -l golang -v ${version} -c '{"Args":["init","HTLCoin","htc","100000000"]}' -P 'OR ('\''Org1MSP.peer'\'','\''Org2MSP.peer'\'')'

    # install htlc
    docker exec -it cli peer chaincode install -n ${FABRIC_HTLC} -v ${version} -p github.com/chaincode/htlc/
    docker exec -it cli peer chaincode instantiate -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n ${FABRIC_HTLC} -l golang -v ${version} -c '{"Args":["init",'"\"${FABRIC_LEDGER}\"","\"mychannel\"","\"${BCOS_HTLC}\""']}'

    # approve
    docker exec -it cli peer chaincode invoke -C mychannel -n ${FABRIC_LEDGER} -c '{"Args":["createEscrowAccount","1000000"]}' -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
}

update_wecross_config()
{
    LOG_INFO "Update wecross.toml ..."

    cat >>routers-payment/127.0.0.1-8250-25500/conf/wecross.toml<<EOF

[[htlc]]
    selfPath = 'payment.bcos.htlc'
    account1 = 'bcos_default_account'
    counterpartyPath = 'payment.fabric.htlc'
    account2 = 'fabric_default_account'
EOF

    cat >>routers-payment/127.0.0.1-8251-25501/conf/wecross.toml<<EOF

[[htlc]]
    selfPath = 'payment.fabric.htlc'
    account1 = 'fabric_default_account'
    counterpartyPath = 'payment.bcos.htlc'
    account2 = 'bcos_default_account'
EOF
}

update_chains_config()
{
    LOG_INFO "Update stub.toml ..."
    cat >>routers-payment/127.0.0.1-8250-25500/conf/chains/bcos/stub.toml<<EOF

[[resources]]
    # name cannot be repeated
    name = 'htlc'
    type = 'BCOS_CONTRACT' # BCOS_CONTRACT or BCOS_SM_CONTRACT
    contractAddress = '${BCOS_HTLC}'
EOF

    cat >>routers-payment/127.0.0.1-8251-25501/conf/chains/fabric/stub.toml<<EOF

[[resources]]
    # name cannot be repeated
    name = '${FABRIC_HTLC}'
    type = 'FABRIC_CONTRACT'
    chainCodeName = '${FABRIC_HTLC}'
    chainLanguage = "go"
    peers=['org1']
EOF
}

copy_console()
{
    LOG_INFO "Copy console for router-8251 ..."

    cp -r WeCross-Console WeCross-Console-8251
    rm WeCross-Console-8251/conf/application.toml
    cat << EOF > WeCross-Console-8251/conf/application.toml
[connection]
    server =  '127.0.0.1:8251'
    sslKey = 'classpath:ssl.key'
    sslCert = 'classpath:ssl.crt'
    caCert = 'classpath:ca.crt'
EOF
}

restart_router()
{
    # print running env
    netstat -napl|grep 20200

    LOG_INFO "Restart routers ..."

    cd ${ROOT}/routers-payment/127.0.0.1-8250-25500/
    bash stop.sh
    bash start.sh

    cd ${ROOT}/routers-payment/127.0.0.1-8251-25501/
    bash stop.sh
    bash start.sh

    cd ${ROOT}
}

main()
{
    config_bcos_sender_account
    restart_router

    init_bcos_htlc

    copy_fabric_chaincode
    install_fabric_chaincode

    update_wecross_config
    # update_chains_config

    copy_console
    restart_router

    LOG_INFO "Config htlc successfully!\n"
    LOG_INFO "Now, you can make a cross-chain transfer by WeCross console using following command!\n"
    echo -e "[BCOS  user ]: newHTLCProposal payment.bcos.htlc bcos_sender bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 9dda9a5e175a919ee98ff0198927b0a765ef96cf917144b589bb8e510e04843c true 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org1.example.com 500 2000000000
\n[Fabric user]: newHTLCProposal payment.fabric.${FABRIC_HTLC} fabric_admin bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 null false 0x55f934bcbe1e9aef8337f5551142a442fdde781c 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org1.example.com 500 2000000000
"
}

main
