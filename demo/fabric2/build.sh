#!/bin/bash
set -e
LANG=en_US.UTF-8
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO][Fabric] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR][Fabric] ${content}\033[0m"
}

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -#LO ${url}
    fi
}

Download_IMG() {
    local name=${1}
    local tag=${2}

    if [ -z "$(docker images | grep ${name} | grep ${tag})" ]; then
        docker pull ${name}:${tag}
        docker tag ${name}:${tag} ${name}:latest
    fi
}

check_docker_service() {
    set +e
    if ! docker ps >/dev/null; then
        LOG_INFO "Please install docker and add your user by:"
        LOG_INFO "        sudo gpasswd -a ${USER} docker && su ${USER}"
        exit 1
    fi
    set -e
}

check_docker_service
fabric_version=2.3.0
samples_version=2.3.0
ca_version=1.4.9
# Download
LOG_INFO "Download fabric tools ..."
if [ "$(uname)" == "Darwin" ]; then
    # Mac
    Download https://github.com/hyperledger/fabric/releases/download/v${fabric_version}/hyperledger-fabric-darwin-amd64-${fabric_version}.tar.gz
    Download https://github.com/hyperledger/fabric-ca/releases/download/v${ca_version}/hyperledger-fabric-ca-darwin-amd64-${ca_version}.tar.gz
else
    Download https://github.com/hyperledger/fabric/releases/download/v${fabric_version}/hyperledger-fabric-linux-amd64-${fabric_version}.tar.gz
    Download https://github.com/hyperledger/fabric-ca/releases/download/v${ca_version}/hyperledger-fabric-ca-linux-amd64-${ca_version}.tar.gz
fi

LOG_INFO "Download fabric samples ..."
Download https://github.com/hyperledger/fabric-samples/archive/v${samples_version}.tar.gz

LOG_INFO "Pull fabric images ..."
Download_IMG hyperledger/fabric-ca ${ca_version}
Download_IMG hyperledger/fabric-ccenv ${fabric_version}
Download_IMG hyperledger/fabric-orderer ${fabric_version}
Download_IMG hyperledger/fabric-peer ${fabric_version}
Download_IMG hyperledger/fabric-tools ${fabric_version}

# Install
LOG_INFO "Install ..."
tar -zxf v${samples_version}.tar.gz
if [ "$(uname)" == "Darwin" ]; then
    # Mac
    tar -zxf hyperledger-fabric-darwin-amd64-${fabric_version}.tar.gz
    tar -zxf hyperledger-fabric-ca-darwin-amd64-${ca_version}.tar.gz
else
    tar -zxf hyperledger-fabric-linux-amd64-${fabric_version}.tar.gz
    tar -zxf hyperledger-fabric-ca-linux-amd64-${ca_version}.tar.gz
fi
if [ -d fabric-samples-${samples_version}/bin ]; then
    LOG_INFO "Bin file already exists ..."
else
    mv -f bin config -t fabric-samples-${samples_version}/
fi
# Startup
LOG_INFO "Startup test-network"
cd fabric-samples-${samples_version}/test-network
bash network.sh up createChannel -c mychannel -i ${fabric_version}
bash network.sh deployCC -ccn sacc -ccp ../chaincode/sacc/ -ccl go <<EOF
Y
EOF
LOG_INFO "Startup test-network done"
cd -

# Dump cert files
# copy cert
# If you run the full "Wecross" demo, open the comments below!

certs_dir=certs
fabric_stub_dir=${certs_dir}/chains/fabric2
fabric_admin_dir=${certs_dir}/accounts/fabric2_admin
fabric_user_dir=${certs_dir}/accounts/fabric2_user1
fabric_admin_org1_dir=${certs_dir}/accounts/fabric2_admin_org1
fabric_admin_org2_dir=${certs_dir}/accounts/fabric2_admin_org2

fabric_verifiers_dir=${certs_dir}/verifiers
fabric_verifiers_org1CA_dir=${certs_dir}/verifiers/org1CA
fabric_verifiers_org2CA_dir=${certs_dir}/verifiers/org2CA
fabric_verifiers_ordererCA_dir=${certs_dir}/verifiers/ordererCA

mkdir -p ${certs_dir} ${fabric_admin_dir} ${fabric_user_dir} ${fabric_stub_dir} ${fabric_admin_org1_dir} ${fabric_admin_org2_dir} \
        ${fabric_verifiers_dir} ${fabric_verifiers_org1CA_dir} ${fabric_verifiers_org2CA_dir} ${fabric_verifiers_ordererCA_dir}

crypto_dir=fabric-samples-2.3.0/test-network/organizations/
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk ${fabric_admin_dir}/account.key
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem ${fabric_admin_dir}/account.crt

cp ${crypto_dir}/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore/*_sk ${fabric_user_dir}/account.key
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/User1@org1.example.com-cert.pem ${fabric_user_dir}/account.crt

cp ${crypto_dir}/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem ${fabric_stub_dir}/orderer-tlsca.crt
cp ${crypto_dir}/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt ${fabric_stub_dir}/org1-tlsca.crt
cp ${crypto_dir}/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt ${fabric_stub_dir}/org2-tlsca.crt

cp ${crypto_dir}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk ${fabric_admin_org1_dir}/account.key
cp ${crypto_dir}/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem ${fabric_admin_org1_dir}/account.crt

cp ${crypto_dir}/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/keystore/*_sk ${fabric_admin_org2_dir}/account.key
cp ${crypto_dir}/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/signcerts/Admin@org2.example.com-cert.pem ${fabric_admin_org2_dir}/account.crt

# cp ${crypto_dir}/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem ${fabric_verifiers_org1CA_dir}/ca.org1.example.com-cert.pem
# cp ${crypto_dir}/peerOrganizations/org2.example.com/ca/ca.org2.example.com-cert.pem ${fabric_verifiers_org2CA_dir}/ca.org2.example.com-cert.pem
# cp ${crypto_dir}/ordererOrganizations/example.com/ca/ca.example.com-cert.pem ${fabric_verifiers_ordererCA_dir}/ca.example.com-cert.pem

LOG_INFO "SUCCESS: Build Fabric demo finish."
