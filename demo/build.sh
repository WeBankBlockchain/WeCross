#!/bin/bash
set -e
LANG=en_US.UTF-8
ROOT=$(pwd)

DB_IP=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123456
BCOS_VERSION=''
BUILD_FROM_SOURCE=''

need_db_config_ask=true

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
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

check_docker_service() {
    set +e
    if ! docker ps >/dev/null; then
        LOG_INFO "Please install docker and add your user by:"
        echo -e "\033[32m        sudo gpasswd -a ${USER} docker && su ${USER}\033[0m"
        exit 1
    fi
    set -e
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

check_port_available() {
    port=$1
    name=$2
    if [ "$(lsof -i:$port | wc -l)" -ne "0" ]; then
        LOG_ERROR "${name} port ${port} is not available. Are there any other blockchain or application is running?"
        exit 1
    fi
}

check_account_manager_available() {
    check_port_available 8340 WeCross-Account-Manager
}

check_fabric_available() {
    check_port_available 7050 Fabric-Orderer
    check_port_available 7051 Fabric-Peer
    check_port_available 8051 Fabric-Peer
    check_port_available 9051 Fabric-Peer
    check_port_available 10051 Fabric-Peer
}

check_bcos_available() {
    # 30300,20200,8545
    check_port_available 30300 BCOS-P2P
    check_port_available 20200 BCOS-Channel
    check_port_available 8545 BCOS-RPC

    check_port_available 30301 BCOS-P2P
    check_port_available 20201 BCOS-Channel
    check_port_available 8546 BCOS-RPC
}

check_wecross_available() {
    check_port_available 8250 WeCross-8250-25500
    check_port_available 25500 WeCross-8250-25500
    check_port_available 8251 WeCross-8251-25501
    check_port_available 25501 WeCross-8251-25501
}

check_env() {
    LOG_INFO "Check environments"
    check_command java
    check_command docker
    check_command docker-compose
    check_command mysql
    check_docker_service
    check_fabric_available
    check_bcos_available
    check_wecross_available
    check_account_manager_available
}

build_bcos() {
    LOG_INFO "Build BCOS ..."
    cd ${ROOT}/bcos
    bash build.sh "${BCOS_VERSION}"

    cd ${ROOT}
}

build_fabric() {
    LOG_INFO "Build Fabric ..."
    cd ${ROOT}/fabric
    bash build.sh
    cd ${ROOT}/
}

check_process() {
    local process_name=${1}
    if [ -z "$(ps -ef | grep ${process_name} | grep -v grep)" ]; then
        LOG_ERROR "Build demo failed: ${process_name} does not exist."
        exit 1
    fi
}

check_container() {
    local container_name=${1}
    if [ -z "$(docker ps | grep ${container_name} | grep -v grep)" ]; then
        LOG_ERROR "Build demo failed: ${container_name} does not exist."
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
    fabric_demo_dir=${2}
    bcos_demo_dir=${3}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}

    # stubs
    bash add_chain.sh -t BCOS2.0 -n bcos -d conf/chains
    # copy cert
    cp ${ROOT}/bcos/nodes/127.0.0.1/sdk/* conf/chains/bcos/

    # deploy system contracts
    bash deploy_system_contract.sh -t BCOS2.0 -c chains/bcos -P
    bash deploy_system_contract.sh -t BCOS2.0 -c chains/bcos -H

    cd -
}

config_router_8251() {
    router_dir=${1}
    fabric_demo_dir=${2}

    LOG_INFO "Configure router ${router_dir}"

    cd ${router_dir}
    # stubs
    bash add_chain.sh -t Fabric1.4 -n fabric -d conf/chains
    cp ${fabric_demo_dir}/certs/chains/fabric/* conf/chains/fabric/

    # fabric stub internal accounts
    bash add_account.sh -t Fabric1.4 -n fabric_admin -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin/* conf/accounts/fabric_admin/
    bash add_account.sh -t Fabric1.4 -n fabric_admin_org1 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin_org1/* conf/accounts/fabric_admin_org1/
    bash add_account.sh -t Fabric1.4 -n fabric_admin_org2 -d conf/accounts
    cp ${fabric_demo_dir}/certs/accounts/fabric_admin_org2/* conf/accounts/fabric_admin_org2/
    sed_i 's/Org1MSP/Org2MSP/g' conf/accounts/fabric_admin_org2/account.toml

    # deploy system chaincodes
    bash deploy_system_contract.sh -t Fabric1.4 -c chains/fabric -P
    bash deploy_system_contract.sh -t Fabric1.4 -c chains/fabric -H

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
            bash download_wecross.sh -t ${WECROSS_VERSION} ${BUILD_FROM_SOURCE}
        else
            bash <(curl -sL https://${GIT_URL_BASE}/WebankBlockchain/WeCross/releases/download/resources/download_wecross.sh) -t ${WECROSS_VERSION} ${BUILD_FROM_SOURCE}
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
            bash download_console.sh -t "${WECROSS_CONSOLE_VERSION}" ${BUILD_FROM_SOURCE}
        else
            bash <(curl -sL https://${GIT_URL_BASE}/WebankBlockchain/WeCross/releases/download/resources/download_console.sh) -t "${WECROSS_CONSOLE_VERSION}" ${BUILD_FROM_SOURCE}
        fi
    fi

    # Build WeCross Console
    LOG_INFO "Build WeCross Console ..."
    cp routers-payment/cert/sdk/* ${ROOT}/WeCross-Console/conf/

    # config universal account
    cat <<EOF >${ROOT}/WeCross-Console/conf/application.toml
[connection]
    server =  '127.0.0.1:8250'
    sslKey = 'classpath:ssl.key'
    sslCert = 'classpath:ssl.crt'
    caCert = 'classpath:ca.crt'
    sslSwitch = 2 # disable ssl:2, SSL without client auth:1 , SSL with client and server auth: 0
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
            bash download_account_manager.sh -t "${WECROSS_ACCOUNT_MANAGER_VERSION}" -u ${DB_USERNAME} -p ${DB_PASSWORD} -H ${DB_IP} -P ${DB_PORT} ${BUILD_FROM_SOURCE}
        else
            bash <(curl -sL https://${GIT_URL_BASE}/WebankBlockchain/WeCross/releases/download/resources/download_account_manager.sh) -t "${WECROSS_ACCOUNT_MANAGER_VERSION}" -u ${DB_USERNAME} -p ${DB_PASSWORD} -H ${DB_IP} -P ${DB_PORT} ${BUILD_FROM_SOURCE}
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
bcosDeploy payment.bcos.HelloWorld conf/contracts/solidity/HelloWorld.sol HelloWorld 1.0
quit
EOF
    cd -
}

deploy_fabric_sample_resource() {
    # deploy from 8250
    LOG_INFO "Deploy fabric chaincode sacc"
    cd ${ROOT}/WeCross-Console/
    sed_i 's/8250/8251/g' conf/application.toml

    bash start.sh <<EOF
login
setDefaultAccount Fabric1.4 2
login
fabricInstall payment.fabric.sacc Org2 contracts/chaincode/sacc 1.0 GO_LANG
setDefaultAccount Fabric1.4 1
login
fabricInstall payment.fabric.sacc Org1 contracts/chaincode/sacc 1.0 GO_LANG
fabricInstantiate payment.fabric.sacc ["Org1","Org2"] contracts/chaincode/sacc 1.0 GO_LANG policy.yaml ["a","10"]
quit
EOF
    # wait the chaincode instantiate
    try_times=120
    i=0
    echo -e "\033[32msacc chaincode is instantiating ...\033[0m\c"
    while [ ! -n "$(docker ps | grep sacc | awk '{print $1}')" ]; do
        sleep 1

        ((i = i + 1))
        if [ $i -lt ${try_times} ]; then
            echo -e "\033[32m.\033[0m\c"
        else
            # LOG_ERROR "Instantiate sacc timeout!"
            exit 1
        fi
    done

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

add_fabric_account() {
    local name=${1}
    local mspid=${2}

    # addChainAccount
    cd ${ROOT}/WeCross-Console/
    bash start.sh <<EOF
login
addChainAccount Fabric1.4 conf/accounts/${name}/account.crt conf/accounts/${name}/account.key ${mspid} true
quit
EOF
    cd -
}

deploy_chain_account() {
    mkdir -p ${ROOT}/WeCross-Console/conf/accounts/
    cd ${ROOT}/WeCross-Console/conf/accounts/ && rm -rf $(ls | grep -v .sh) && cd -
    cp -r ${ROOT}/bcos/accounts/* ${ROOT}/WeCross-Console/conf/accounts/
    cp -r ${ROOT}/fabric/certs/accounts/* ${ROOT}/WeCross-Console/conf/accounts/

    add_bcos_account bcos_user1                  # 0
    add_fabric_account fabric_admin_org1 Org1MSP # 1
    add_fabric_account fabric_admin_org2 Org2MSP # 2
    # add_fabric_account fabric_user1 Org1MSP      # 3
    # add_bcos_gm_account bcos_gm_user1 # 4
}

deploy_sample_resource() {
    deploy_fabric_sample_resource
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

    # Build Fabric
    build_fabric

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

    deploy_chain_account

    deploy_sample_resource

    LOG_INFO "Success! WeCross demo network is running. Framework:"
    echo -e "
            FISCO BCOS                    Fabric
           (4node pbft)              (first-network)
          (HelloWorld.sol)              (sacc.go)
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
Create a wecross demo with bcos and fabric chains.
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
    while getopts "H:P:u:p:df:hs" option; do
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
        f)
            BCOS_VERSION=$OPTARG
            ;;
        s)
            BUILD_FROM_SOURCE='-s'
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
