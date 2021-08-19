#!/bin/bash

set -e
LANG=en_US.UTF-8

counter=0
zone=
ip_array=
rpc_port_array=
p2p_port_array=
peers_array=
use_file=
ip_param=
ip_file=
ca_dir=
ca=0
enable_test_resource="false"
make_tar=0
router_output=$(pwd)/routers
wecross_dir=$(dirname $(pwd)/${0})/

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

# shellcheck disable=SC2120
help() {
    echo "$1"
    cat <<EOF
Usage:
    -n  <zone id>                   [Required]   set zone ID
    -l  <ip:rpc-port:p2p-port>      [Optional]   "ip:rpc-port:p2p-port" e.g:"127.0.0.1:8250:25500"
    -f  <ip list file>              [Optional]   split by line, every line should be "ip:rpc-port:p2p-port". eg "127.0.0.1:8250:25500"
    -c  <ca dir>                    [Optional]   dir of existing ca
    -o  <output dir>                [Optional]   default ${router_output}/
    -z  <generate tar packet>       [Optional]   default no
    -T  <enable test mode>          [Optional]   default no. Enable test resource.
    -h  call for help
e.g
    bash $0 -n payment -l 127.0.0.1:8250:25500
    bash $0 -n payment -f ipfile
    bash $0 -n payment -f ipfile -c ./ca/
EOF
    exit 0
}

check_env() {
    # shellcheck disable=SC2143
    # shellcheck disable=SC2236
    [ ! -z "$(openssl version | grep 1.0.2)" ] || [ ! -z "$(openssl version | grep 1.1)" ] || [ ! -z "$(openssl version | grep reSSL)" ] || {
        LOG_ERROR "Please install openssl!"
        #echo "download openssl from https://www.openssl.org."
        LOG_INFO "Use \"openssl version\" command to check."
        exit 1
    }

    if [ ! -z "$(openssl version | grep reSSL)" ]; then
        export PATH="/usr/local/opt/openssl/bin:$PATH"
    fi

    if [ "$(uname)" == "Darwin" ]; then
        macOS="macOS"
    fi

    if [ "$(uname -m)" != "x86_64" ]; then
        x86_64_arch="false"
    fi
}

parse_command() {
    while getopts "o:n:l:f:c:zTh" option; do
        # shellcheck disable=SC2220
        case ${option} in
        o)
            router_output=$OPTARG
            ;;
        n)
            zone=$OPTARG
            ;;
        l)
            use_file=0
            ip_param=$OPTARG
            ;;
        f)
            use_file=1
            ip_file=$OPTARG
            ;;
        c)
            ca=1
            ca_dir=$OPTARG
            ;;
        z)
            make_tar=1
            ;;
        T)
            enable_test_resource="true"
            ;;
        h) help ;;
        esac
    done
}

check_params() {
    if [ -z "${zone}" ]; then
        LOG_ERROR "Please set [zone id]"
        help
        exit 1
    fi

    if [ -z "${use_file}" ]; then
        LOG_ERROR "Please set [ip:rpc-port:p2p-Port]"
        help
        exit 1
    fi

    if [ -d ${router_output} ]; then
        LOG_ERROR "Output dir: ${router_output} exists. Please remove the dir."
        exit 1
    fi

    if [ ${ca} -eq 1 ] && [ ! -f "${ca_dir}"/ca.crt ]; then
        LOG_ERROR "Flie ${ca_dir}/ca.crt doesn't exist. Please check the ca path."
        exit 1
    fi
}

gen_crt() {
    scripts_dir=${1}/
    output=${2}/
    num=${3}

    # generate ca.crt
    if [ ${ca} -eq 0 ]; then
        bash "${scripts_dir}"/create_cert.sh -c -d "${output}" 2>/dev/null
        ca_dir=${output}
    fi

    # generate sdk cert
    bash "${scripts_dir}"/create_cert.sh -n -D "${ca_dir}" -d "${output}/sdk" 2>/dev/null

    # generate node cert by number
    bash "${scripts_dir}"/create_cert.sh -n -C "${num}" -D "${ca_dir}" -d "${output}" 2>/dev/null

    rm -f cert.cnf
    echo "================================================================"
}

#index ip rpc_port p2p_port peers
gen_one_router() {
    #default execute dir: ../WeCross
    cert_dir=${1}
    output=${router_output}/${2}-${3}-${4}
    target=${2}-${3}-${4}

    # mkdir
    mkdir -p ${output}/
    mkdir -p ${output}/conf/accounts
    mkdir -p ${output}/conf/chains
    mkdir -p ${output}/plugin
    mkdir -p ${output}/pages
    mkdir -p ${wecross_dir}/pages

    # copy files
    chmod u+x ${wecross_dir}./*.sh
    cp -r ${wecross_dir}./add_account.sh "${output}/"
    cp -r ${wecross_dir}./add_chain.sh "${output}/"
    cp -r ${wecross_dir}./deploy_system_contract.sh "${output}/"
    cp -r ${wecross_dir}./start.sh "${output}/"
    cp -r ${wecross_dir}./stop.sh "${output}/"
    cp -r ${wecross_dir}/apps "${output}/"
    cp -r ${wecross_dir}/lib "${output}/"

    # Configure plugin
    cp -r ${wecross_dir}/plugin ${output}/

    # Configure pages
    cp -r ${wecross_dir}/pages ${output}/

    cp -r "${wecross_dir}/conf" "${output}/"
    cp -r "${cert_dir}"/* "${output}"/conf/
    gen_conf "${output}"/conf/wecross.toml "${2}" "${3}" "${4}" "${5}"
    LOG_INFO "Create ${output} successfully"

    if [ ${make_tar} -eq 1 ]; then
        cd "${router_output}"
        tar -czf "${target}".tar.gz "${target}"
        # cp "${target}".tar.gz ../
        cd ..
        LOG_INFO "Create ${output}.tar.gz successfully"
    fi
}

gen_conf() {
    cat <<EOF >"${1}"
[common]
    zone = '${zone}'
    visible = true
    enableAccessControl = false

[chains]
    path = 'classpath:chains'

[rpc] # rpc ip & port
    address = '127.0.0.1'
    port = ${3}
    caCert = 'classpath:ca.crt'
    sslCert = 'classpath:ssl.crt'
    sslKey = 'classpath:ssl.key'
    sslSwitch = 2  # disable ssl:2, SSL without client auth:1 , SSL with client and server auth: 0
    webRoot = 'classpath:pages'
    mimeTypesFile = 'classpath:conf/mime.types' # set the content-types of a file

[p2p]
    listenIP = '0.0.0.0'
    listenPort = ${4}
    caCert = 'classpath:ca.crt'
    sslCert = 'classpath:ssl.crt'
    sslKey = 'classpath:ssl.key'
    peers = [${5}]

[account-manager]
    server =  '127.0.0.1:8340'
    admin = 'org1-admin'
    password = '123456'
    sslKey = 'classpath:ssl.key'
    sslCert = 'classpath:ssl.crt'
    caCert = 'classpath:ca.crt'

#[[htlc]]
#    selfPath = 'payment.bcos.htlc'
#    account1 = 'bcos_default_account'
#    counterpartyPath = 'payment.fabric.htlc'
#    account2 = 'fabric_default_account'

EOF
}

format_ip_file() {
    local file=${1}
    if [ $(tail -n1 ${file} | wc -l ) -eq 0 ]; then
        # add '\n' to the end of file if not exist
        echo "" >> ${file}
    fi
}

parse_ip_file() {
    # shellcheck disable=SC2162
    while read line; do
        ip_array[counter]=$(echo "${line}" | awk -F ':' '{print $1}')
        rpc_port_array[counter]=$(echo "${line}" | awk -F ':' '{print $2}')
        p2p_port_array[counter]=$(echo "${line}" | awk -F ':' '{print $3}')
        if [ -z "${ip_array[counter]}" ] && [ -z "${rpc_port_array[counter]}" ] && [ -z "${p2p_port_array[counter]}" ]; then
            ((--counter))
        elif [ -z "${ip_array[counter]}" ] || [ -z "${rpc_port_array[counter]}" ] || [ -z "${p2p_port_array[counter]}" ]; then
            LOG_ERROR "Please check ${1} format! e.g:\n127.0.0.1:8250:25500\n127.0.0.1:8251:25501\n127.0.0.2:8252:25502"
            exit 1
        fi
        ((++counter))
    done <"${1}"
}

# shellcheck disable=SC2120
gen_some_routers() {
    certs_dir_prefix=${1}
    for ((i = 0; i < counter; i++)); do
        for ((j = 0; j < counter; j++)); do
            # shellcheck disable=SC2057
            if [ ${i} -ne ${j} ]; then
                peers_array[i]=${peers_array[i]}"'"${ip_array[j]}:${p2p_port_array[j]}"'",
            fi
        done

        peers_array[i]=$(echo "${peers_array[i]}" | awk '{sub(/.$/,"")}1')
        gen_one_router "${certs_dir_prefix}${i}" "${ip_array[i]}" "${rpc_port_array[i]}" "${p2p_port_array[i]}" "${peers_array[i]}"
    done
}

gen_scripts() {
    cp ${wecross_dir}/start_all.sh ${router_output}/
    cp ${wecross_dir}/stop_all.sh ${router_output}/
    cp ${wecross_dir}/create_cert.sh ${router_output}/cert/
}

main() {
    check_params

    if [ ${use_file} -eq 0 ]; then
        ip_rpc_p2p=(${ip_param//:/ })
        gen_crt ${wecross_dir} "$router_output"/cert/ 1
        gen_one_router "$router_output"/cert/node0 "${ip_rpc_p2p[0]}" "${ip_rpc_p2p[1]}" "${ip_rpc_p2p[2]}"
        gen_scripts
    elif [ ${use_file} -eq 1 ]; then
        format_ip_file ${ip_file}
        parse_ip_file "${ip_file}"
        gen_crt ${wecross_dir} "$router_output"/cert/ ${counter}
        gen_some_routers "$router_output"/cert/node
        gen_scripts
    else
        help
    fi
}

print_result() {
    LOG_INFO "All completed. WeCross routers are generated in: ${router_output}/"
}

# shellcheck disable=SC2068
parse_command $@
check_env
main
print_result
