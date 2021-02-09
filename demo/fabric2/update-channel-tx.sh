#!/bin/bash
set -e
LANG=en_US.UTF-8

CHANNEL_NAME=$1
echo "Channel name : "$CHANNEL_NAME
export PATH=${PWD}/../bin:${PWD}:$PATH
export FABRIC_CFG_PATH=${PWD}

configtxgen -profile TwoOrgsChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME
res=$?
if [ $res -ne 0 ]; then
echo "Failed to generate channel configuration transaction..."
exit 1
fi

configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org1MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org1MSP
res=$?
if [ $res -ne 0 ]; then
echo "Failed to generate anchor peer update for Org1MSP..."
exit 1
fi

configtxgen -profile TwoOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/Org2MSPanchors.tx -channelID $CHANNEL_NAME -asOrg Org2MSP
res=$?
if [ $res -ne 0 ]; then
echo "Failed to generate anchor peer update for Org2MSP..."
exit 1
fi