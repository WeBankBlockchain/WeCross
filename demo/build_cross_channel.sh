#!/bin/bash
set -e
LANG=en_US.UTF-8
ROOT=$(pwd)

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

version_file="profile_version.sh"
[[ ! -f "${version_file}" ]] && {
  LOG_ERROR " ${version_file} not exist, please check if the demo is the latest. "
  exit 1
}

source "${version_file}"
LOG_INFO "WeCross Version: ${WECROSS_VERSION}"

Download()
{
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -#LO ${url}
    fi
}

sed_i()
{
    if [ "$(uname)" == "Darwin" ]; then
    # Mac
        sed -i "" $@
    else
        sed -i $@
    fi
}

check_command()
{
    local cmd=${1}
    if [ -z "$(command -v ${cmd})" ];then
        LOG_ERROR "${cmd} is not installed."
        exit 1
    fi
}

check_docker_service()
{
    set +e
    docker ps > /dev/null
    if [ "$?" -ne "0" ]; then
        LOG_INFO "Please install docker and add your user by:"
        echo -e "\033[32m        sudo gpasswd -a ${USER} docker && su ${USER}\033[0m"
        exit 1
    fi
    set -e
}

check_port_available()
{
    port=$1
    name=$2
    if [ "$(lsof -i:$port | wc -l)" -ne "0" ]; then
        LOG_ERROR "${name} port ${port} is not available. Are there any other blockchain is running?"
        exit 1
    fi
}

check_fabric_available()
{
    #7050，7051，8051，9051，10051
    check_port_available 7050 Fabric-Orderer
    check_port_available 7051 Fabric-Peer
    check_port_available 8051 Fabric-Peer
    check_port_available 9051 Fabric-Peer
    check_port_available 10051 Fabric-Peer
}

check_wecross_available()
{
    check_port_available 8250 WeCross-8250-25500
    check_port_available 25500 WeCross-8250-25500
    check_port_available 8251 WeCross-8251-25501
    check_port_available 25501 WeCross-8251-25501
}


check_env()
{
    LOG_INFO "Check environments"
    check_command java
    check_command docker
    check_command docker-compose
    check_docker_service
    check_fabric_available
    check_wecross_available
}


build_fabric()
{
    LOG_INFO "Build Fabric ..."
    cd ${ROOT}/fabric
    bash build.sh
    #add_channel
    CHANNEL_NAME=yourchannel
    chmod +x add_channel.sh
    chmod +x update-channel-tx.sh
    cp -f update-channel-tx.sh fabric-samples-1.4.4/first-network
    cd ./fabric-samples-1.4.4/first-network/
    ./update-channel-tx.sh $CHANNEL_NAME
    cd -
    docker cp add_channel.sh cli:/opt/gopath/src/github.com/hyperledger/fabric/peer/scripts/add_channel.sh
    docker exec cli scripts/add_channel.sh $CHANNEL_NAME
    cd ${ROOT}/
}


check_process()
{
    local process_name=${1}
    if [ -z "$(ps -ef |grep ${process_name} |grep -v grep)" ];then
        LOG_ERROR "Build demo failed: ${process_name} does not exist."
        exit 1
    fi
}

check_container()
{
    local container_name=${1}
    if [ -z "$(docker ps |grep ${container_name} |grep -v grep)" ];then
        LOG_ERROR "Build demo failed: ${container_name} does not exist."
        exit 1
    fi
}

check_fabric()
{
    check_container peer0.org1.example.com
    check_container peer1.org1.example.com
    check_container peer0.org2.example.com
    check_container peer1.org2.example.com
    check_container orderer.example.com
}

check_wecross()
{
    check_process routers-payment/127.0.0.1-8250-25500
    check_process routers-payment/127.0.0.1-8251-25501
}

check_wecross_network()
{
    check_wecross
}

clear_ask()
{

    # Clear history
    if [ -e ${ROOT}/routers-payment ];then
        read -p "Old demo network exist. Clear all and re-build? [Y/n]" ans
        case "$ans" in
        y | Y | "")
            LOG_INFO "Clear old network ..."
            bash clear.sh
        ;;
        *)
            exit 0
        ;;
        esac

    fi
}

console_ask()
{
    read -p "Start WeCross console? [Y/n]" ans
    case "$ans" in
    y | Y | "")
    cd ${ROOT}/WeCross-Console && ./start.sh
    ;;
    *)
    echo "To start WeCross console. Just: \"cd ./WeCross-Console && ./start.sh\""
    ;;
    esac
}

config_router_8250()
{
    router_dir=${1}
    fabric_demo_dir=${2}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # account

    bash add_account.sh -t Fabric1.4 -n fabric_admin -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin/* conf/accounts/fabric_admin/
    bash add_account.sh -t Fabric1.4 -n fabric_admin_org1 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin_org1/* conf/accounts/fabric_admin_org1/
    bash add_account.sh -t Fabric1.4 -n fabric_admin_org2 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin_org2/* conf/accounts/fabric_admin_org2/
    sed_i  's/Org1MSP/Org2MSP/g'  conf/accounts/fabric_admin_org2/account.toml
    bash add_account.sh -t Fabric1.4 -n fabric_default_account -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_user1/* conf/accounts/fabric_default_account/
    bash add_account.sh -t Fabric1.4 -n fabric_user1 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_user1/* conf/accounts/fabric_user1/

    # stubs
    bash add_chain.sh -t Fabric1.4 -n fabric -d conf/chains
    cp ${fabric_demo_dir}/certs/chains/fabric/* conf/chains/fabric/
    sed_i  's/payment/payment1/g'  conf/wecross.toml

    # deploy proxy
    bash deploy_system_contract.sh -t Fabric1.4 -c chains/fabric -P

    cd -
}

config_router_8251()
{
    router_dir=${1}
    fabric_demo_dir=${2}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # account

    bash add_account.sh -t Fabric1.4 -n fabric_admin -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin/* conf/accounts/fabric_admin/
    bash add_account.sh -t Fabric1.4 -n fabric_admin_org1 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin_org1/* conf/accounts/fabric_admin_org1/
    bash add_account.sh -t Fabric1.4 -n fabric_admin_org2 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin_org2/* conf/accounts/fabric_admin_org2/
    sed_i  's/Org1MSP/Org2MSP/g'  conf/accounts/fabric_admin_org2/account.toml
    bash add_account.sh -t Fabric1.4 -n fabric_default_account -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_user1/* conf/accounts/fabric_default_account/
    bash add_account.sh -t Fabric1.4 -n fabric_user2 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_user1/* conf/accounts/fabric_user2/

    # stubs
    bash add_chain.sh -t Fabric1.4 -n fabric -d conf/chains
    cp ${fabric_demo_dir}/certs/chains/fabric/* conf/chains/fabric/
    sed_i  's/mychannel/yourchannel/g'  conf/chains/fabric/stub.toml
    sed_i  's/payment/payment2/g'  conf/wecross.toml


    # deploy proxy
    bash deploy_system_contract.sh -t Fabric1.4 -c chains/fabric -P

    cd -
}

download_wecross()
{
    # Download
    LOG_INFO "Download WeCross ..."
    if [ -e download_wecross.sh ];then
        bash download_wecross.sh -t "${WECROSS_VERSION}"
    else
        bash <(curl -sL https://${GIT_URL_BASE}/WebankBlockchain/WeCross/releases/download/resources/download_wecross.sh) -t "${WECROSS_VERSION}"
    fi
}

download_wecross_console()
{
    LOG_INFO "Download WeCross Console ..."
    if [ -e download_console.sh ];then
        bash download_console.sh -t "${WECROSS_CONSOLE_VERSION}"
    else
        bash <(curl -sL https://${GIT_URL_BASE}/WebankBlockchain/WeCross/releases/download/resources/download_console.sh) -t "${WECROSS_CONSOLE_VERSION}"
    fi
}

deploy_channel1_sample_resource()
{
    # deploy from 8250
    LOG_INFO "Deploy fabric chaincode sacc1 to channel1"
    cd ${ROOT}/WeCross-Console/
    
    bash start.sh <<EOF
    fabricInstall payment1.fabric.sacc1 fabric_admin_org1 Org1 contracts/chaincode/sacc 1.0 GO_LANG
    fabricInstall payment1.fabric.sacc1 fabric_admin_org2 Org2 contracts/chaincode/sacc 1.0 GO_LANG
    fabricInstantiate payment1.fabric.sacc1 fabric_admin ["Org1","Org2"] contracts/chaincode/sacc 1.0 GO_LANG policy.yaml ["a","10"]
quit
EOF
    # wait the chaincode instantiate
    try_times=80
    i=0
    echo -e "\033[32msacc1 chaincode is instantiating ...\033[0m\c"
    while [ ! -n "$(docker ps |grep sacc1 |awk '{print $1}')" ]
    do
        sleep 1

        ((i=i+1))
        if [ $i -lt ${try_times} ]; then
            echo -e "\033[32m.\033[0m\c"
        else
            LOG_ERROR "Instantiate sacc1 timeout!"
            exit 1
        fi
    done
    cd -
}

deploy_channel2_sample_resource()
{
    # deploy from 8250
    LOG_INFO "Deploy fabric chaincode sacc2 to channel2"
    cd ${ROOT}/WeCross-Console/

    sed_i  's/8250/8251/g'  conf/application.toml
    bash start.sh <<EOF
    fabricInstall payment2.fabric.sacc2 fabric_admin_org1 Org1 contracts/chaincode/sacc 1.0 GO_LANG
    fabricInstall payment2.fabric.sacc2 fabric_admin_org2 Org2 contracts/chaincode/sacc 1.0 GO_LANG
    fabricInstantiate payment2.fabric.sacc2 fabric_admin ["Org1","Org2"] contracts/chaincode/sacc 1.0 GO_LANG policy.yaml ["a","10"]
quit
EOF
    # wait the chaincode instantiate
    try_times=80
    i=0
    echo -e "\033[32msacc2 chaincode is instantiating ...\033[0m\c"
    while [ ! -n "$(docker ps |grep sacc2 |awk '{print $1}')" ]
    do
        sleep 1

        ((i=i+1))
        if [ $i -lt ${try_times} ]; then
            echo -e "\033[32m.\033[0m\c"
        else
            LOG_ERROR "Instantiate sacc2 timeout!"
            exit 1
        fi
    done
    sed_i  's/8251/8250/g'  conf/application.toml
    cd -
}

deploy_sample_resource()
{
    deploy_channel1_sample_resource
    deploy_channel2_sample_resource
}

main()
{
    clear_ask

    check_env

    download_wecross
    download_wecross_console

    # Build Routers
    LOG_INFO "Build Routers ..."
    cat << EOF > ipfile
127.0.0.1:8250:25500
127.0.0.1:8251:25501
EOF
    bash ./WeCross/build_wecross.sh -n payment -o routers-payment -f ipfile

    # Build WeCross Console
    LOG_INFO "Build WeCross Console ..."
    cp routers-payment/cert/sdk/* ${ROOT}/WeCross-Console/conf/
    cp ${ROOT}/WeCross-Console/conf/application-sample.toml ${ROOT}/WeCross-Console/conf/application.toml

    cd ${ROOT}/WeCross-Console/
    bash start.sh <<EOF
quit
EOF
    cd ${ROOT}/

    # Build FABRIC
    build_fabric

    # config routers
    config_router_8250 ${ROOT}/routers-payment/127.0.0.1-8250-25500/ ${ROOT}/fabric
    config_router_8251 ${ROOT}/routers-payment/127.0.0.1-8251-25501/ ${ROOT}/fabric

    # Start up routers
    cd ${ROOT}/routers-payment/127.0.0.1-8250-25500/
    bash start.sh

    cd ${ROOT}/routers-payment/127.0.0.1-8251-25501/
    bash start.sh

    cd ${ROOT}

    check_wecross_network

    deploy_sample_resource
    

    LOG_INFO "Success! WeCross demo network is running. Framework:"
    echo -e "
                      Hyperledger Fabric
              Channel 1                  Channel 2
         (payment1_Channel1)        (payment2_Channel2)
             (sacc1.go)                 (sacc2.go)
                 |                          |
                 |                          |
                 |                          |
          WeCross Router <----------> WeCross Router <----------> WeCross Account Manager
      (127.0.0.1-8250-25500)      (127.0.0.1-8251-25501)             (127.0.0.1:8340)
          /            \\
         /              \\
        /                \\
 WeCross WebApp     WeCross Console
"

}

main
if [ ! -n "$1" ] ;then
    console_ask
fi
