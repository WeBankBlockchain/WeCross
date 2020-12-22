#!/bin/bash
set -e
LANG=en_US.UTF-8
ROOT=$(pwd)

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

check_process() {
    local process_name=${1}
    if [ -z "$(ps -ef | grep ${process_name} | grep -v grep)" ]; then
        LOG_ERROR "Process ${process_name} does not exist. Please check."
        exit 1
    fi
}

check_container() {
    local container_name=${1}
    if [ -z "$(docker ps | grep ${container_name} | grep -v grep)" ]; then
        LOG_ERROR "Container ${container_name} does not exist. Please check."
        exit 1
    fi
}

check_bcos() {
    check_process bcos/nodes/127.0.0.1/node0/../fisco-bcos
    check_process bcos/nodes/127.0.0.1/node1/../fisco-bcos
}

check_fabric() {
    check_container peer0.org1.example.com
    check_container peer1.org1.example.com
    check_container peer0.org2.example.com
    check_container peer1.org2.example.com
    check_container orderer.example.com
}

check_wecross() {
    check_process routers-payment/127.0.0.1-8250-25500
    check_process routers-payment/127.0.0.1-8251-25501
}

check_wecross_network() {
    check_bcos
    check_fabric
    check_wecross
}

sed_i() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        sed -i "" $@
    else
        sed -i $@
    fi
}

deploy_fabric_xa_chaincodes() {
    # deploy from 8250
    LOG_INFO "Deploy Fabric xa chaincodes: payment.fabric.evidence"
    cd ${ROOT}/WeCross-Console/
    sed_i 's/8250/8251/g' conf/application.toml

    bash start.sh <<EOF
    login
    setDefaultAccount Fabric1.4 2
    login
    fabricInstall payment.fabric.evidence Org2 contracts/chaincode/evidence 1.0 GO_LANG
    setDefaultAccount Fabric1.4 1
    login
    fabricInstall payment.fabric.evidence Org1 contracts/chaincode/evidence 1.0 GO_LANG
    fabricInstantiate payment.fabric.evidence ["Org1","Org2"] contracts/chaincode/evidence 1.0 GO_LANG default []
    quit
EOF
    # wait the chaincode instantiate
    try_times=90
    i=0
    echo -e "\033[32mevidence chaincode is instantiating ...\033[0m\c"
    while [ ! -n "$(docker ps | grep evidence | awk '{print $1}')" ]; do
        sleep 1

        ((i = i + 1))
        if [ $i -lt ${try_times} ]; then
            echo -e "\033[32m.\033[0m\c"
        else
            LOG_ERROR "Instantiate payment.fabric.evidence timeout!"
            exit 1
        fi
    done
    cd -
}

deploy_bcos_xa_contracts() {
    # deploy from 8250
    LOG_INFO "Deploy BCOS xa contract: payment.bcos.evidence"
    cd ${ROOT}/WeCross-Console/
    sed_i 's/8251/8250/g' conf/application.toml

    bash start.sh <<EOF
    login
    bcosDeploy payment.bcos.evidence conf/contracts/solidity/EvidenceSample.sol Evidence 1.0
    quit
EOF
    cd -
}

console_ask() {
    read -r -p "Start WeCross Console to try? [Y/n]" ans
    case "$ans" in
    y | Y | "")
        cd ${ROOT}/WeCross-Console && ./start.sh
        ;;
    *)
        echo "To start WeCross console. Just: \"cd ./WeCross-Console && ./start.sh\""
        ;;
    esac
}

main() {
    check_wecross_network
    deploy_fabric_xa_chaincodes
    deploy_bcos_xa_contracts
    LOG_INFO "SUCCESS: xa evidence example has been deployed to FISCO BCOS and Fabric:"
    echo -e "
      FISCO BCOS                    Fabric
\033[32m(payment.bcos.evidence)   (payment.fabric.evidence)\033[0m
           |                          |
           |                          |
    WeCross Router <----------> WeCross Router
(127.0.0.1-8250-25500)      (127.0.0.1-8251-25501)
           |
           |
    WeCross Console
"
}

main $@
if [ ! -n "$1" ]; then
    console_ask
fi
