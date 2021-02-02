#!/bin/bash
set -e
LANG=en_US.UTF-8
ROOT=$(pwd)
DB_IP=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123456

need_db_config_ask=true

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

Download() {
    local url=${1}
    local file=$(basename ${url})
    if [ ! -e ${file} ]; then
        curl -#LO ${url}
    fi
}

sed_i() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        sed -i "" $@
    else
        sed -i $@
    fi
}

check_command() {
    local cmd=${1}
    if [ -z "$(command -v ${cmd})" ]; then
        LOG_ERROR "${cmd} is not installed."
        exit 1
    fi
}

query_db() {
    if [ ${DB_PASSWORD} ]; then
        mysql -u ${DB_USERNAME} --password="${DB_PASSWORD}" -h ${DB_IP} -P ${DB_PORT} $@ 2>/dev/null
    else
        mysql -u ${DB_USERNAME} -h ${DB_IP} -P ${DB_PORT} $@ 2>/dev/null
    fi
}

check_db_service() {
    LOG_INFO "Checking database configuration"
    set +e
    if ! query_db -e "status;" >/dev/null; then
        LOG_ERROR "Database configuration error."
        LOG_INFO "Please config database, username and password. And use this command to check:"
        echo -e "\033[32m        mysql -u ${DB_USERNAME} --password=\"<your password>\" -h ${DB_IP} -P ${DB_PORT} -e \"status;\" \033[0m"
        exit 1
    fi
    set -e
    LOG_INFO "Database configuration OK!"
}

check_port_avaliable() {
    port=$1
    name=$2
    if [ "$(lsof -i:$port | wc -l)" -ne "0" ]; then
        LOG_ERROR "${name} port ${port} is not avaliable. Are there any other blockchain is running?"
        exit 1
    fi
}

check_account_manager_avaliable() {
    check_port_avaliable 8340 WeCross-Account-Manager
}

check_bcos_avaliable() {
    # 30300,20200,8545
    check_port_avaliable 30300 BCOS-P2P
    check_port_avaliable 20200 BCOS-Channel
    check_port_avaliable 8545 BCOS-RPC

    check_port_avaliable 30301 BCOS-P2P
    check_port_avaliable 20201 BCOS-Channel
    check_port_avaliable 8546 BCOS-RPC
}

check_wecross_avaliable() {
    check_port_avaliable 8250 WeCross-8250-25500
    check_port_avaliable 25500 WeCross-8250-25500
    check_port_avaliable 8251 WeCross-8251-25501
    check_port_avaliable 25501 WeCross-8251-25501
}

check_env() {
    LOG_INFO "Check environments"
    check_command java
    check_command mysql
    check_bcos_avaliable
    check_wecross_avaliable
    check_account_manager_avaliable
}

build_bcos() {
    LOG_INFO "Build BCOS ..."
    cd ${ROOT}/bcos

    # Setting to build 2 groups
    cat <<EOF >ipconf
127.0.0.1:4 agency1 1,2
EOF

    bash build.sh

    cd ${ROOT}
}

check_process() {
    local process_name=${1}
    if [ -z "$(ps -ef | grep ${process_name} | grep -v grep)" ]; then
        LOG_ERROR "Build demo failed: ${process_name} does not exist."
        exit 1
    fi
}

check_bcos() {
    check_process bcos/nodes/127.0.0.1/node0/../fisco-bcos
    check_process bcos/nodes/127.0.0.1/node1/../fisco-bcos
}

check_wecross() {
    check_process routers-payment/127.0.0.1-8250-25500
    check_process routers-payment/127.0.0.1-8251-25501
}

check_wecross_network() {
    check_bcos
    check_wecross
}

clear_ask() {

    # Clear history
    if [ -e ${ROOT}/routers-payment ]; then
        read -r -p "Old demo network exist. Clear all and re-build? [Y/n]" ans
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

console_ask() {
    read -r -p "Start WeCross Console? [Y/n]" ans
    case "$ans" in
    y | Y | "")
        cd ${ROOT}/WeCross-Console && ./start.sh
        ;;
    *)
        echo "To start WeCross console. Just: \"cd ./WeCross-Console && ./start.sh\""
        ;;
    esac
}

exit_when_empty_db_pwd() {
    if mysql -u ${DB_USERNAME} -h ${DB_IP} -P ${DB_PORT} -e "status" 2>/dev/null; then
        LOG_ERROR "Not support to use account with no password. Please try another account."
        exit 1
    fi
}

db_config_ask() {
    check_command mysql
    LOG_INFO "Database connection:"
    read -r -p "[1/4]> ip: " DB_IP
    read -r -p "[2/4]> port: " DB_PORT
    read -r -p "[3/4]> username: " DB_USERNAME
    exit_when_empty_db_pwd
    read -r -p "[4/4]> password: " -s DB_PASSWORD
    echo "" # \n
    LOG_INFO "Database connetion with: ${DB_IP}:${DB_PORT} ${DB_USERNAME} "
    check_db_service
}

config_router_8250() {
    router_dir=${1}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}

    # stubs
    bash add_chain.sh -t BCOS2.0 -n group1 -d conf/chains
    # copy cert
    cp ${ROOT}/bcos/nodes/127.0.0.1/sdk/* conf/chains/group1/

    # deploy system contracts
    bash deploy_system_contract.sh -t BCOS2.0 -c chains/group1 -P
    bash deploy_system_contract.sh -t BCOS2.0 -c chains/group1 -H

    cd -
}

config_router_8251() {
    router_dir=${1}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # stubs
    bash add_chain.sh -t BCOS2.0 -n group2 -d conf/chains
    # copy cert
    cp ${ROOT}/bcos/nodes/127.0.0.1/sdk/* conf/chains/group2/

    # configure to group2
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        sed -i "" 's/groupId = 1/groupId = 2/g' conf/chains/group2/stub.toml
    else
        sed -i 's/groupId = 1/groupId = 2/g' conf/chains/group2/stub.toml
    fi

    # deploy system contracts
    bash deploy_system_contract.sh -t BCOS2.0 -c chains/group2 -P
    bash deploy_system_contract.sh -t BCOS2.0 -c chains/group2 -H

    cd -
}

config_database() {
    if ${need_db_config_ask}; then
        db_config_ask
    else
        check_db_service
    fi
}

build_wecross() {
    # Download
    LOG_INFO "Download WeCross ..."

    local name=./WeCross
    if [ -d "${name}" ]; then
        LOG_INFO "${name} exists."
    else
        if [ -e download_wecross.sh ]; then
            bash download_wecross.sh -t v1.0.1
        else
            bash <(curl -sL https://github.com/WebankBlockchain/WeCross/releases/download/resources/download_wecross.sh) -t v1.0.1
        fi
    fi

    # Build Routers
    LOG_INFO "Build Routers ..."
    cat <<EOF >ipfile
127.0.0.1:8250:25500
127.0.0.1:8251:25501
EOF
    bash ./WeCross/build_wecross.sh -n payment -o routers-payment -f ipfile
}

build_wecross_console() {
    LOG_INFO "Download WeCross Console ..."

    local name=./WeCross-Console
    if [ -d "${name}" ]; then
        LOG_INFO "${name} exists."
    else
        if [ -e download_console.sh ]; then
            bash download_console.sh -t v1.0.1
        else
            bash <(curl -sL https://github.com/WebankBlockchain/WeCross/releases/download/resources/download_console.sh) -t v1.0.1
        fi
    fi

    # Build WeCross Console
    LOG_INFO "Build WeCross Console ..."
    cp routers-payment/cert/sdk/* ${ROOT}/WeCross-Console/conf/
    cp ${ROOT}/WeCross-Console/conf/application-sample.toml ${ROOT}/WeCross-Console/conf/application.toml

    # config universal account
    cat <<EOF >>${ROOT}/WeCross-Console/conf/application.toml
[login]
    username = 'org1-admin'
    password = '123456'
EOF

    cd ${ROOT}/WeCross-Console/
    bash start.sh <<EOF
quit
EOF
    cd -
}

build_account_manager() {
    LOG_INFO "Download WeCross Account Manager ..."

    local name=./WeCross-Account-Manager
    if [ -d ${name} ]; then
        LOG_INFO "${name} exists."
    else
        if [ -e download_account_manager.sh ]; then
            bash download_account_manager.sh -t v1.0.1 -u ${DB_USERNAME} -p ${DB_PASSWORD} -H ${DB_IP} -P ${DB_PORT}
        else
            bash <(curl -sL https://github.com/WebankBlockchain/WeCross/releases/download/resources/download_account_manager.sh) -t v1.0.1 -u ${DB_USERNAME} -p ${DB_PASSWORD} -H ${DB_IP} -P ${DB_PORT}
        fi
    fi

    # Build Account Manager
    LOG_INFO "Build WeCross Account Manager ..."
    cp routers-payment/cert/sdk/* ${ROOT}/WeCross-Account-Manager/conf/
    cp ${ROOT}/WeCross-Account-Manager/conf/application-sample.toml ${ROOT}/WeCross-Account-Manager/conf/application.toml
    sed_i "/jdbc/s/localhost/${DB_IP}/g" ${ROOT}/WeCross-Account-Manager/conf/application.toml
    sed_i "/jdbc/s/3306/${DB_PORT}/g" ${ROOT}/WeCross-Account-Manager/conf/application.toml
    sed_i "/username/s/root/${DB_USERNAME}/g" ${ROOT}/WeCross-Account-Manager/conf/application.toml
    sed_i "/password/s/''/'${DB_PASSWORD}'/g" ${ROOT}/WeCross-Account-Manager/conf/application.toml

    sed_i 's/update/create/g' ${ROOT}/WeCross-Account-Manager/conf/application.properties

    LOG_INFO "Setup database"
    cd ${ROOT}/WeCross-Account-Manager/
    query_db <conf/db_setup.sql

    # generate rsa_keypair
    bash create_rsa_keypair.sh -d conf/

    bash start.sh
    sed_i 's/create/update/g' ${ROOT}/WeCross-Account-Manager/conf/application.properties
}

deploy_bcos_sample_resource() {
    # deploy from 8250
    LOG_INFO "Deploy bcos contract HelloWorld"
    cd ${ROOT}/WeCross-Console/
    sed_i 's/8251/8250/g' conf/application.toml

    bash start.sh <<EOF
    login
    bcosDeploy payment.group1.HelloWorldGroup1 conf/contracts/solidity/HelloWorld.sol HelloWorld 1.0
    bcosDeploy payment.group2.HelloWorldGroup2 conf/contracts/solidity/HelloWorld.sol HelloWorld 1.0
    quit
EOF
    cd -
}

add_bcos_account() {
    local name=${1}

    # get address
    cd ${ROOT}/WeCross-Console/conf/accounts/${name}/
    local address=$(ls 0x*.public.pem | awk -F "." '{print $1}')
    cd -

    # addChainAccount
    cd ${ROOT}/WeCross-Console/
    bash start.sh <<EOF
    login
    addChainAccount BCOS2.0 conf/accounts/${name}/${address}.public.pem conf/accounts/${name}/${address}.pem ${address} true
    quit
EOF
    cd -

}

add_bcos_gm_account() {
    local name=${1}

    # get address
    cd ${ROOT}/WeCross-Console/conf/accounts/${name}/
    local address=$(ls 0x*.public.pem | awk -F "." '{print $1}')
    cd -

    # addChainAccount
    cd ${ROOT}/WeCross-Console/
    bash start.sh <<EOF
    login
    addChainAccount GM_BCOS2.0 conf/accounts/${name}/${address}.public.pem conf/accounts/${name}/${address}.pem ${address} true
    quit
EOF
    cd -

}

deploy_chain_account() {
    mkdir -p ${ROOT}/WeCross-Console/conf/accounts/
    cd ${ROOT}/WeCross-Console/conf/accounts/ && rm -rf $(ls | grep -v .sh) && cd -
    cp -r ${ROOT}/bcos/accounts/* ${ROOT}/WeCross-Console/conf/accounts/

    add_bcos_account bcos_user1 # 0
    # add_bcos_gm_account bcos_gm_user1 # 4
}

deploy_sample_resource() {
    deploy_bcos_sample_resource
}

main() {
    clear_ask

    check_env

    config_database

    build_wecross
    build_wecross_console
    build_account_manager

    # Build BCOS
    build_bcos

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

    deploy_chain_account

    deploy_sample_resource

    LOG_INFO "Success! WeCross demo network is running. Framework:"
    echo -e "
                          FISCO BCOS
              Group 1                    Group 2
         (HelloWorldGroup1)         (HelloWorldGroup2)
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

help() {
    echo "$1"
    cat <<EOF
Create a wecross demo with two bcos groups.
Usage:
    -d                              [Optional] Use default db configuration: -H ${DB_IP} -P ${DB_PORT} -u ${DB_USERNAME} -p ${DB_PASSWORD}
    -H                              [Optional] DB ip
    -P                              [Optional] DB port
    -u                              [Optional] DB username
    -p                              [Optional] DB password
    -h  call for help
e.g
    bash $0 -H ${DB_IP} -P ${DB_PORT} -u ${DB_USERNAME} -p 123456
    bash $0
EOF
    exit 0
}

parse_command() {
    while getopts "H:P:u:p:dh" option; do
        # shellcheck disable=SC2220
        case ${option} in
        d)
            need_db_config_ask=false
            ;;
        H)
            DB_IP=$OPTARG
            need_db_config_ask=false
            ;;
        P)
            DB_PORT=$OPTARG
            need_db_config_ask=false
            ;;
        u)
            DB_USERNAME=$OPTARG
            need_db_config_ask=false
            ;;
        p)
            DB_PASSWORD=$OPTARG
            need_db_config_ask=false
            ;;
        h) help ;;
        *) help ;;
        esac
    done
}

parse_command "$@"

main "$@"

if [ ! -n "$1" ]; then
    console_ask
fi
