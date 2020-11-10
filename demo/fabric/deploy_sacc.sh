#!/bin/bash

# initialize sacc
docker exec -i cli peer chaincode install -n sacc -v 1.0 -p github.com/chaincode/sacc/
docker exec -i cli peer chaincode instantiate -o orderer.example.com:7050 --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n sacc -l golang -v 1.0 -c '{"Args":["a","10"]}' -P 'OR ('\''Org1MSP.peer'\'','\''Org2MSP.peer'\'')'
