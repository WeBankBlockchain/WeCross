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

check_port_avaliable()
{
    port=$1
    name=$2
    if [ "$(lsof -i:$port | wc -l)" -ne "0" ]; then
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
}


check_env()
{
    LOG_INFO "Check environments"
    check_command java
    check_bcos_avaliable
    check_wecross_avaliable
}

build_bcos()
{
    LOG_INFO "Build BCOS ..."
    cd ${ROOT}/bcos
    bash build.sh

    cd ${ROOT}
}

check_process()
{
    local process_name=${1}
    if [ -z "$(ps -ef |grep ${process_name} |grep -v grep)" ];then
        LOG_ERROR "Build demo failed: ${process_name} does not exist."
        exit 1
    fi
}


check_bcos()
{
    check_process bcos/nodes/127.0.0.1/node0/../fisco-bcos
}

check_wecross()
{
    check_process routers-payment/127.0.0.1-8250-25500
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

config_router_8250()
{
    router_dir=${1}
    fabric_demo_dir=${2}
    bcos_demo_dir=${3}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # account
    bash add_account.sh -t BCOS2.0 -n bcos_default_account -d conf/accounts
    bash add_account.sh -t BCOS2.0 -n bcos_user1 -d conf/accounts

    # stubs
    bash add_chain.sh -t BCOS2.0 -n bcos -d conf/chains
    # copy cert
    cp ${ROOT}/bcos/nodes/127.0.0.1/sdk/* conf/chains/bcos/

    # deploy proxy
    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.proxy.ProxyContractDeployment deploy chains/bcos bcos_user1

    cd -
}


download_wecross()
{
    # Download
    LOG_INFO "Download WeCross ..."

    local name=./WeCross
    if [ -d "${name}"  ]; then
        LOG_INFO "${name} exists."
    else
        if [ -e download_wecross.sh ];then
            bash download_wecross.sh -t v1.0.0-rc4
        else
            bash <(curl -sL https://github.com/WeBankFinTech/WeCross/releases/download/resources/download_wecross.sh) -t v1.0.0-rc4
        fi
    fi
}

download_wecross_console()
{
    LOG_INFO "Download WeCross Console ..."

    local name=./WeCross-Console
    if [ -d "${name}"  ]; then
        LOG_INFO "${name} exists."
    else
        if [ -e download_console.sh ];then
            bash download_console.sh -t v1.0.0-rc4
        else
            bash <(curl -sL https://github.com/WeBankFinTech/WeCross/releases/download/resources/download_console.sh) -t v1.0.0-rc4
        fi
    fi
}

deploy_bcos_sample_resource()
{
    LOG_INFO "Download HelloWeCross.sol ..."
    Download https://github.com/WeBankFinTech/WeCross/releases/download/resources/HelloWeCrossV2.sol
    cp HelloWeCrossV2.sol ${ROOT}/WeCross-Console/conf/contracts/solidity/

    # deploy from 8250
    LOG_INFO "Deploy bcos contract HelloWorld"

    cd ${ROOT}/WeCross-Console/
    sed_i  's/8251/8250/g'  conf/application.toml

    bash start.sh <<EOF
bcosDeploy payment.bcos.HelloWeCross bcos_user1 conf/contracts/solidity/HelloWeCrossV2.sol HelloWeCross 1.0
quit
EOF
    cd -
}

deploy_sample_resource()
{
    deploy_bcos_sample_resource
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
    build_bcos

    # config routers
    config_router_8250 ${ROOT}/routers-payment/127.0.0.1-8250-25500/ ${ROOT}/fabric ${ROOT}/bcos

    # Start up routers
    cd ${ROOT}/routers-payment/127.0.0.1-8250-25500/
    bash start.sh

    cd ${ROOT}

    check_wecross_network

    deploy_sample_resource

    LOG_INFO "Success! WeCross bcos single network is running."
        echo -e "
      FISCO BCOS
   (HelloWeCross.sol)
           |
           |
    WeCross Router
(127.0.0.1-8250-25500)
           |
           |
    WeCross Console
"
}

main
