#!/bin/bash
set -e
LANG=en_US.UTF-8
ROOT=$(pwd)

version=$RANDOM
BCOS_LEDGER=
BCOS_HTLC=
FABRIC_LEDGER=ledger
FABRIC_HTLC=htlc
DOCKER_ID=

## address = 0x4305196480b029bbecb071b4b68e95dfef36a7b7
BCOS_SENDER_SK=$(echo "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR0VBZ0VBTUJBR0J5cUdTTTQ5QWdFR0JTdUJCQUFLQkcwd2F3SUJBUVFnQ2l3eFNQSTg5S2lmeUMxNjRueDYKQmtKc1hUZE9Qck1oOW1sOGlBdzYzK2VoUkFOQ0FBUXEwazFYZEpsQjNSeUNFbnZJb1g5VFZBUUt2YnRTVXVtTQpIelgyY3Q2SWIydFg1ZVUzM2F2RG5hejlWWlJOK0RyOUJVYkYyRUdjbzlDdnlPV2pHQWNOCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K" | base64 -d)
BCOS_SENDER_PK=$(echo "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZZd0VBWUhLb1pJemowQ0FRWUZLNEVFQUFvRFFnQUVLdEpOVjNTWlFkMGNnaEo3eUtGL1UxUUVDcjI3VWxMcApqQjgxOW5MZWlHOXJWK1hsTjkycnc1MnMvVldVVGZnNi9RVkd4ZGhCbktQUXI4amxveGdIRFE9PQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K" | base64 -d)

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

config_org2_admin() {
    LOG_INFO "Config account for BCOS htlc sender ..."
    mkdir ${ROOT}/WeCross-Console/conf/accounts/bcos_sender
    cd ${ROOT}/WeCross-Console/conf/accounts/bcos_sender

    cat >>0x4305196480b029bbecb071b4b68e95dfef36a7b7.public.pem <<EOF
${BCOS_SENDER_PK}
EOF
    cat >>0x4305196480b029bbecb071b4b68e95dfef36a7b7.pem <<EOF
${BCOS_SENDER_SK}
EOF

    # addChainAccount
    cd ${ROOT}/WeCross-Console
    bash start.sh <<EOF
    registerAccount org2-admin 123456
    login org2-admin 123456
    addChainAccount BCOS2.0 conf/accounts/bcos_sender/0x4305196480b029bbecb071b4b68e95dfef36a7b7.public.pem conf/accounts/bcos_sender/0x4305196480b029bbecb071b4b68e95dfef36a7b7.pem 0x4305196480b029bbecb071b4b68e95dfef36a7b7 true
    quit
EOF
    cd -
}

init_bcos_htlc() {
    LOG_INFO "Init BCOS htlc ..."

    cd ${ROOT}/WeCross-Console
    rm -rf logs
    bash start.sh <<EOF
  login org2-admin 123456
  bcosDeploy payment.bcos.ledger contracts/solidity/LedgerSample.sol LedgerSample ${version} htlc token 1 100000000
quit
EOF
    var1=$(cat logs/debug.log | grep data=0x | awk 'END{print $11}')
    BCOS_LEDGER=$(echo ${var1:5:42})

    rm -rf logs
    bash start.sh <<EOF
  login org2-admin 123456
  bcosDeploy payment.bcos.htlc contracts/solidity/LedgerSampleHTLC.sol LedgerSampleHTLC ${version}
quit
EOF

    var2=$(cat logs/debug.log | grep data=0x | awk 'END{print $11}')
    BCOS_HTLC=$(echo ${var2:5:42})

    bash start.sh <<EOF
  login org2-admin 123456
  sendTransaction payment.bcos.ledger approve ${BCOS_HTLC} 1000000
  sendTransaction payment.bcos.htlc init ${BCOS_LEDGER}
quit
EOF
    cd -
}

copy_fabric_chaincode() {
    LOG_INFO "Copy Fabric chaincode ..."

    docker ps -a | grep cli >tmp.txt
    DOCKER_ID=$(grep 'fabric' tmp.txt | awk '{print $1}')

    docker cp ${ROOT}/WeCross-Console/conf/contracts/chaincode/ledger ${DOCKER_ID}:/opt/gopath/src/github.com/chaincode/ledger
    docker cp ${ROOT}/WeCross-Console/conf/contracts/chaincode/htlc ${DOCKER_ID}:/opt/gopath/src/github.com/chaincode/htlc
}

install_fabric_chaincode() {
    LOG_INFO "Install Fabric chaincode ..."

    # install ledger
    docker exec -i cli peer chaincode install -n ${FABRIC_LEDGER} -v ${version} -p github.com/chaincode/ledger/
    docker exec -i cli peer chaincode instantiate -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n ${FABRIC_LEDGER} -l golang -v ${version} -c '{"Args":["init","HTLCoin","htc","100000000"]}' -P 'OR ('\''Org1MSP.peer'\'','\''Org2MSP.peer'\'')'

    # install htlc
    docker exec -i cli peer chaincode install -n ${FABRIC_HTLC} -v ${version} -p github.com/chaincode/htlc/
    docker exec -i cli peer chaincode instantiate -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n ${FABRIC_HTLC} -l golang -v ${version} -c '{"Args":["init",'"\"${FABRIC_LEDGER}\""',"mychannel"]}'

    # approve
    docker exec -i cli peer chaincode invoke -C mychannel -n ${FABRIC_LEDGER} -c '{"Args":["createEscrowAccount","1000000"]}' -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
}

update_wecross_config() {
    LOG_INFO "Update wecross.toml ..."

    cat >>${ROOT}/routers-payment/127.0.0.1-8250-25500/conf/wecross.toml <<EOF

[[htlc]]
    selfPath = 'payment.bcos.htlc'
    counterpartyPath = 'payment.fabric.htlc'
EOF

    cat >>${ROOT}/routers-payment/127.0.0.1-8251-25501/conf/wecross.toml <<EOF

[[htlc]]
    selfPath = 'payment.fabric.htlc'
    counterpartyPath = 'payment.bcos.htlc'
EOF
}


restart_router() {
    LOG_INFO "Restart routers ..."

    cd ${ROOT}/routers-payment/127.0.0.1-8250-25500/
    bash stop.sh
    bash start.sh

    cd ${ROOT}/routers-payment/127.0.0.1-8251-25501/
    bash stop.sh
    bash start.sh

    cd ${ROOT}
}

main() {

    config_org2_admin

    init_bcos_htlc

    copy_fabric_chaincode
    install_fabric_chaincode

    update_wecross_config

    restart_router

    LOG_INFO "Config htlc successfully!\n"
    LOG_INFO "Now, you can make a cross-chain transfer by WeCross console using following command!\n"
    echo -e "[BCOS  user ]: newHTLCProposal payment.bcos.htlc bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 9dda9a5e175a919ee98ff0198927b0a765ef96cf917144b589bb8e510e04843c true 0x4305196480b029bbecb071b4b68e95dfef36a7b7 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org2.example.com 500 2000000000
\n[Fabric user]: newHTLCProposal payment.fabric.${FABRIC_HTLC} bea2dfec011d830a86d0fbeeb383e622b576bb2c15287b1a86aacdba0a387e11 null false 0x4305196480b029bbecb071b4b68e95dfef36a7b7 0x2b5ad5c4795c026514f8317c7a215e218dccd6cf 700 2000010000 Admin@org1.example.com User1@org2.example.com 500 2000000000
"
}

main
