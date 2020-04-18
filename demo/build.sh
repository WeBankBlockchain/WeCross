#!/bin/bash
set -e
LANG=en_US.utf8
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

Download()
{
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -LO ${url}
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
    docker ps > /dev/null
}

check_env()
{
    check_command java
    check_command docker
    check_command docker-compose
    check_docker_service
}

build_bcos()
{
    LOG_INFO "Build BCOS ..."
    cd ${ROOT}/bcos 
    bash build.sh

    hello_address=$(grep 'HelloWorld' console/deploylog.txt | awk 'END {print $5}')

    cd ${ROOT}
}


config_bcos_stub_toml()
{
    local file=${1}
    local contractAddress=${2}

    # delete resources sample
    local start=$(grep -rn 'resources is' ${file} | awk -F ":" '{print $1}')
    local end=$(wc -l ${file} |awk '{print $1}')
    sed_i "${start},${end}d" ${file} #delete line: [start, end]

    # add real sample
    cat << EOF > ${file}
$(cat ${file})
# resources is a list
[[resources]]
    # name must be unique
    name = 'HelloWorld'
    type = 'BCOS_CONTRACT'
    contractAddress = '${contractAddress}'
EOF

}

build_fabric()
{
    LOG_INFO "Build Fabric ..."
    cd ${ROOT}/fabric
    bash build.sh
    cd ${ROOT}/
}

check_process()
{
    local process_name=${1}
    if [ -z "$(ps -ef |grep ${process_name} |grep -v grep)" ];then
        LOG_ERROR "Build demo failed: ${process_name} is not exist." 
        exit 1
    fi
}

check_container()
{
    local container_name=${1}
    if [ -z "$(docker ps |grep ${container_name} |grep -v grep)" ];then
        LOG_ERROR "Build demo failed: ${container_name} is not exist." 
        exit 1
    fi
}

check_bcos()
{
    check_process bcos/nodes/127.0.0.1/node0/../fisco-bcos
    check_process bcos/nodes/127.0.0.1/node1/../fisco-bcos
    check_process bcos/nodes/127.0.0.1/node2/../fisco-bcos
    check_process bcos/nodes/127.0.0.1/node3/../fisco-bcos
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
    check_bcos
    check_fabric
    check_wecross
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
    bcos_demo_dir=${3}

    cd ${router_dir}
    # account
    bash add_account.sh -t BCOS2.0 -n bcos_user1 -d conf/accounts

    bash add_account.sh -t Fabric1.4 -n fabric_user1 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_user1/* conf/accounts/fabric_user1/

    # stubs
    bash add_chain.sh -t BCOS2.0 -n bcos -d conf/chains
    # copy cert
    cp ${ROOT}/bcos/nodes/127.0.0.1/sdk/* conf/chains/bcos/
    hello_address=$(grep 'HelloWeCross' ${bcos_demo_dir}/console/deploylog.txt | awk 'END {print $5}')
    # modify stub.toml
    config_bcos_stub_toml conf/chains/bcos/stub.toml ${hello_address}

    cd -
}

config_router_8251()
{
    router_dir=${1}
    fabric_demo_dir=${2}

    cd ${router_dir}
    # account
    bash add_account.sh -t BCOS2.0 -n bcos_user2 -d conf/accounts

    bash add_account.sh -t Fabric1.4 -n fabric_admin -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin/* conf/accounts/fabric_admin/
    bash add_account.sh -t Fabric1.4 -n fabric_user1 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_user1/* conf/accounts/fabric_user1/

    # stubs
    bash add_chain.sh -t Fabric1.4 -n fabric -d conf/chains
    cp ${fabric_demo_dir}/certs/chains/fabric/* conf/chains/fabric/

    cd -
}

main2()
{
    check_env

    # Clear history
    if [ -e ${ROOT}/routers-payment ];then
        LOG_INFO "Clear old network ..."
        bash clear.sh
    fi

    # Download
    LOG_INFO "Download WeCross ..."
    bash <(curl -sL https://github.com/WeBankFinTech/WeCross/releases/download/resources/download_wecross.sh)
    LOG_INFO "Download WeCross Console ..."
    bash <(curl -sL https://github.com/WeBankFinTech/WeCross-Console/releases/download/resources/download_console.sh)

}

main()
{
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

    # Build BCOS
    #build_bcos

    # Build Fabric
    #build_fabric

    # config routers
    config_router_8250 ${ROOT}/routers-payment/127.0.0.1-8250-25500/ ${ROOT}/fabric ${ROOT}/bcos
    config_router_8251 ${ROOT}/routers-payment/127.0.0.1-8251-25501/ ${ROOT}/fabric

    # Start up routers
    cd ${ROOT}/routers-payment/127.0.0.1-8250-25500/
    bash start.sh

    cd ${ROOT}/routers-payment/127.0.0.1-8251-25501/
    bash start.sh

    cd ${ROOT}

    check_wecross_network

    LOG_INFO "Success! WeCross demo network is running. Framework:"
    echo -e "
      FISCO BCOS                    Fabric
     (4node pbft)              (first-network)
   (HelloWorld.sol)               (abac.go)
          |                           |
          |                           |
    WeCross Router <----------> WeCross Router
(127.0.0.1-8250-25500)      (127.0.0.1-8251-25501)
           | 
           | 
    WeCross Console
"
    console_ask
}

main

