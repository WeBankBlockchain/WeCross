#!/bin/bash
set -e
LANG=en_US.utf8
LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO][Fabric] ${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m[ERROR][Fabric] ${content}\033[0m"
}

Download()
{
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -LO ${url}
    fi
}

Download_IMG()
{
    local name=${1}
    local tag=${2}

    if [ -z "$(docker images |grep ${name} |grep ${tag})" ];then
        docker pull ${name}:${tag}
        docker tag ${name}:${tag} ${name}:latest
    fi
}

# Download
LOG_INFO "Download fabric tools ..."
Download https://github.com/hyperledger/fabric/releases/download/v1.4.6/hyperledger-fabric-linux-amd64-1.4.6.tar.gz

LOG_INFO "Download fabric samples ..."
Download https://github.com/hyperledger/fabric-samples/archive/v1.4.4.tar.gz

LOG_INFO "Pull fabric images ..."
Download_IMG hyperledger/fabric-ca 1.4.4
Download_IMG hyperledger/fabric-ccenv 1.4.4
Download_IMG hyperledger/fabric-javaenv 1.4.4
Download_IMG hyperledger/fabric-orderer 1.4.4
Download_IMG hyperledger/fabric-peer 1.4.4
Download_IMG hyperledger/fabric-tools 1.4.4

# Install
LOG_INFO "Install ..."
tar -zxf v1.4.4.tar.gz
tar -zxf hyperledger-fabric-linux-amd64-1.4.6.tar.gz
mv bin fabric-samples-1.4.4/ -f
rm config -rf


# Startup
LOG_INFO "Startup first-network"
cd fabric-samples-1.4.4/first-network
bash byfn.sh up <<EOF
Y
EOF
cd -

# Dump cert files
# copy cert
certs_dir=certs
fabric_stub_dir=${certs_dir}/chains/fabric
fabric_admin_dir=${certs_dir}/accounts/fabric_admin
fabric_user_dir=${certs_dir}/accounts/fabric_user1

mkdir -p ${certs_dir} ${fabric_admin_dir} ${fabric_user_dir} ${fabric_stub_dir}

crypto_dir=fabric-samples-1.4.4/first-network/crypto-config/
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk ${fabric_admin_dir}/account.key
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem ${fabric_admin_dir}/account.crt

cp ${crypto_dir}/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore/*_sk ${fabric_user_dir}/account.key
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem ${fabric_user_dir}/account.crt

cp ${crypto_dir}/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem ${fabric_stub_dir}/orderer-tlsca.crt
cp ${crypto_dir}/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt ${fabric_stub_dir}/org1-tlsca.crt
cp ${crypto_dir}/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt ${fabric_stub_dir}/org2-tlsca.crt



