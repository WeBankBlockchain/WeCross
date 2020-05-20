#!/bin/bash

# initialize sacc
docker exec -it cli peer chaincode install -n sacc -v 1.0 -p github.com/chaincode/sacc/
docker exec -it cli peer chaincode instantiate -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n sacc -l golang -v 1.0 -c '{"Args":["a","10"]}' -P 'OR ('\''Org1MSP.peer'\'','\''Org2MSP.peer'\'')'

echo "Add this resource configuration into corresponding chains/<fabric chain>/stub.toml:

[[resources]]
    # name cannot be repeated
    name = 'abac'
    type = 'FABRIC_CONTRACT'
    chainCodeName = 'mycc'
    chainLanguage = 'go'
    peers=['org1','org2']"