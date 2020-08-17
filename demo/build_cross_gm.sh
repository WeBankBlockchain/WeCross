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
    set +e
    docker ps > /dev/null
    if [ "$?" -ne "0" ]; then
        LOG_INFO "Please install docker and add your user by:"
        echo -e "\033[32m        sudo gpasswd -a ${USER} docker && su ${USER}\033[0m"
        exit 1
    fi
    set -e
}

check_port_avaliable()
{
    port=$1
    name=$2
    if [ "$(netstat -na 2>/dev/null |grep $port | wc -l)" -ne "0" ]; then
        LOG_ERROR "${name} port ${port} is not avaliable. Are there any other blockchain is running?"
        exit 1
    fi
}

check_bcos_avaliable()
{
    # 30300,20200,8545
    check_port_avaliable 30300 BCOS-P2P
    check_port_avaliable 20200 BCOS-Channel
    check_port_avaliable 8545 BCOS-RPC

    check_port_avaliable 30301 BCOS-P2P
    check_port_avaliable 20201 BCOS-Channel
    check_port_avaliable 8546 BCOS-RPC
}

check_wecross_avaliable()
{
    check_port_avaliable 8250 WeCross-8250-25500
    check_port_avaliable 25500 WeCross-8250-25500
    check_port_avaliable 8251 WeCross-8251-25501
    check_port_avaliable 25501 WeCross-8251-25501
}


check_env()
{
    LOG_INFO "Check environments"
    check_command java
    check_command docker
    check_command docker-compose
    check_docker_service
    check_bcos_avaliable
    check_wecross_avaliable
}

build_bcos()
{
    LOG_INFO "Build BCOS ..."
    cd ${ROOT}/bcos

    # Setting to build 2 groups
    cat << EOF > ipconf
127.0.0.1:1 agency1 1
EOF

    bash build.sh
    cd ${ROOT}
}

build_bcos_gm()
{
    LOG_INFO "Build BCOS ..."
    cd ${ROOT}/bcos

    # Setting to build 2 groups
    cat << EOF > ipconf
127.0.0.1:1 agency1 1
EOF

    bash build_gm.sh
    cd ${ROOT}
}


config_bcos_stub_toml()
{
    local file=${1}
    local contractAddress=${2}

    # delete resources sample
    local start=$(grep -n 'resources is' ${file} | awk -F ":" '{print $1}')
    local end=$(wc -l ${file} |awk '{print $1}')
    sed_i "${start},${end}d" ${file} #delete line: [start, end]

    # add real sample
    cat << EOF > ${file}
$(cat ${file})
# resources is a list
[[resources]]
    # name must be unique
    name = 'HelloWeCross'
    type = 'BCOS_CONTRACT'
    contractAddress = '${contractAddress}'
EOF

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

check_bcos()
{
    check_process bcos/nodes/127.0.0.1/node0/../fisco-bcos
    check_process bcos/nodes_gm/127.0.0.1/node0/../fisco-bcos
}

check_wecross()
{
    check_process routers-payment/127.0.0.1-8250-25500
    check_process routers-payment/127.0.0.1-8251-25501
}

check_wecross_network()
{
    check_bcos
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

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # account
    bash add_account.sh -t BCOS2.0 -n bcos_user1 -d conf/accounts
    bash add_account.sh -t GM_BCOS2.0 -n bcos_gm_user1 -d conf/accounts

    # stubs
    bash add_chain.sh -t BCOS2.0 -n bcos -d conf/chains
    # copy cert
    cp ${ROOT}/bcos/nodes/127.0.0.1/sdk/* conf/chains/bcos/

    # deploy proxy
    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.proxy.ProxyContractDeployment deploy chains/bcos bcos_user1

    cd -
}

config_router_8251()
{
    router_dir=${1}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # account
    bash add_account.sh -t BCOS2.0 -n bcos_user2 -d conf/accounts
    bash add_account.sh -t GM_BCOS2.0 -n bcos_gm_user2 -d conf/accounts

    # stubs
    bash add_chain.sh -t GM_BCOS2.0 -n bcos_gm -d conf/chains
    # copy cert
    cp -r ${ROOT}/bcos/nodes_gm/127.0.0.1/sdk/* conf/chains/bcos_gm/

    # configure guomi
    if [ "$(uname)" == "Darwin" ]; then
    # Mac
        sed -i "" 's/20200/20210/g' conf/chains/bcos_gm/stub.toml
    else
        sed -i 's/20200/20210/g' conf/chains/bcos_gm/stub.toml
    fi

    # deploy proxy
    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.proxy.ProxyContractDeployment deploy chains/bcos_gm bcos_gm_user2

    cd -
}

download_wecross()
{
    # Download
    LOG_INFO "Download WeCross ..."
    if [ -e download_wecross.sh ];then
        bash download_wecross.sh -t v1.0.0-rc3
    else
        bash <(curl -sL https://github.com/WeBankFinTech/WeCross/releases/download/resources/download_wecross.sh) -t v1.0.0-rc3
    fi
}

download_wecross_console()
{
    LOG_INFO "Download WeCross Console ..."
    if [ -e download_console.sh ];then
        bash download_console.sh -t v1.0.0-rc3
    else
        bash <(curl -sL https://github.com/WeBankFinTech/WeCross/releases/download/resources/download_console.sh) -t v1.0.0-rc3
    fi
}


deploy_sample_resource()
{
    # deploy from 8250
    LOG_INFO "Deploy bcos contract HelloWorld to group1 and group2"
    cd ${ROOT}/WeCross-Console/
    bash start.sh <<EOF
bcosDeploy payment.bcos.HelloWorld bcos_user1 conf/contracts/solidity/HelloWorld.sol HelloWorld 1.0
bcosDeploy payment.bcos_gm.HelloWorld bcos_gm_user1 conf/contracts/solidity/HelloWorld.sol HelloWorld 1.0
quit
EOF

    cd -
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

    # Build BCOS & GM BCOS
    build_bcos
    build_bcos_gm

    # config routers
    config_router_8250 ${ROOT}/routers-payment/127.0.0.1-8250-25500/
    config_router_8251 ${ROOT}/routers-payment/127.0.0.1-8251-25501/

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
                    FISCO BCOS
         Normal                     Guomi
      (HelloWorld)               (HelloWorld)
           |                          |
           |                          |
    WeCross Router <----------> WeCross Router
(127.0.0.1-8250-25500)      (127.0.0.1-8251-25501)
           | 
           | 
    WeCross Console
"

}

main
if [ ! -n "$1" ] ;then
    console_ask
fi