#!/bin/bash

set -e

counter=0
network=
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
router_output=routers

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

# shellcheck disable=SC2120
help()
{
    echo "$1"
    cat << EOF
Usage:
    -n  <network id>                [Required]   set network ID
    -l  <ip:rpc-port:p2p-port>      [Optional]   "ip:rpc-port:p2p-port" e.g:"127.0.0.1:8250:25500"
    -f  <ip list file>              [Optional]   split by line, every line should be "ip:rpc-port:p2p-port". eg "127.0.0.1:8250:25500"
    -c  <ca dir>                    [Optional]   dir of existing ca
    -o  <output dir>                [Optional]   default ./${router_output}/
    -z  <generate tar packet>       [Optional]   default no
    -T  <enable test mode>          [Optional]   default no. Enable test resource.
    -h  call for help
e.g
    bash $0 -n payment -l 127.0.0.1:8250:25500
    bash $0 -n payment -f ipfile
EOF
exit 0
}

check_env()
{
    # shellcheck disable=SC2143
    # shellcheck disable=SC2236
    [ ! -z "$(openssl version | grep 1.0.2)" ] || [ ! -z "$(openssl version | grep 1.1)" ] || [ ! -z "$(openssl version | grep reSSL)" ] || {
        LOG_ERROR "Please install openssl!"
        #echo "download openssl from https://www.openssl.org."
        LOG_INFO "Use \"openssl version\" command to check."
        exit 1
    }

    if [ ! -z "$(openssl version | grep reSSL)" ];then
        export PATH="/usr/local/opt/openssl/bin:$PATH"
    fi

    if [ "$(uname)" == "Darwin" ];then
        macOS="macOS"
    fi
    
    if [ "$(uname -m)" != "x86_64" ];then
        x86_64_arch="false"
    fi
}

parse_command()
{
while getopts "o:n:l:f:c:zTh" option;do
    # shellcheck disable=SC2220
    case ${option} in
    o)
        router_output=$OPTARG
    ;;
    n)
        network=$OPTARG
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
    h)  help;;
    esac
done
}

check_params()
{
    if [ -z "${network}" ];then
        LOG_ERROR "Please set [Network id]"
        help
        exit 1
    fi

    if [ -z "${use_file}" ];then
        LOG_ERROR "Please set [ip:rpc-port:p2p-Port]"
        help
        exit 1
    fi

    if [ ${ca} -eq 1 ] && [ ! -f "${ca_dir}"/ca.crt ]; then
        LOG_ERROR " Flie ${ca_dir}/ca.crt doesn't exist. Please check the ca path."
        exit 1
    fi
}

gen_crt()
{
    scripts_dir=${1}/
    output=${2}/
    num=${3}

    if [ ${ca} -eq 0 ];then
        bash "${scripts_dir}"/create_cert.sh -c -d "${output}" 2>/dev/null
        ca_dir=${output}
    fi
    # get ca.crt

    # get node.crt by number
    bash "${scripts_dir}"/create_cert.sh -n -C "${num}" -D "${ca_dir}" -d "${output}" 2>/dev/null

    rm -f cert.cnf
    echo "================================================================"
}

#index ip rpc_port p2p_port pers
gen_one_wecross()
{
    #default execute dir: ../WeCross
    cert_dir=${1}
    output=${router_output}/${2}-${3}-${4}
    target=${2}-${3}-${4}

    mkdir -p "${output}/"
    chmod u+x *.sh
    cp -r *.sh "${output}/"
    cp -r apps "${output}/"
    cp -r lib "${output}/"

    mkdir -p "${output}"/conf
    cp -r "${cert_dir}" "${output}"/conf/p2p
    gen_conf "${output}"/conf/wecross.toml "${2}" "${3}" "${4}" "${5}"
    LOG_INFO "Create ${output} successfully"

    if [ ${make_tar} -eq 1 ];then
        cd "${router_output}"
        tar -czf "${target}".tar.gz "${target}"
        # cp "${target}".tar.gz ../
        cd ..
        LOG_INFO "Create ${output}.tar.gz successfully"
    fi
}

gen_conf()
{
    cat << EOF > "${1}"
[common]
    network = '${network}'
    visible = true

[stubs]
    path = 'classpath:stubs'

[server] # rpc ip & port
    address = '${2}'
    port = ${3}

[p2p]
    listenIP = '0.0.0.0'
    listenPort = ${4}
    caCert = 'classpath:p2p/ca.crt'
    sslCert = 'classpath:p2p/node.crt'
    sslKey = 'classpath:p2p/node.key'
    peers = [${5}]

[test]
    enableTestResource = ${enable_test_resource}
EOF
}

parse_ip_file()
{
    # shellcheck disable=SC2162
    while read line;do
        ip_array[counter]=$(echo "${line}" | awk -F ':' '{print $1}')
        rpc_port_array[counter]=$(echo "${line}" | awk -F ':' '{print $2}')
        p2p_port_array[counter]=$(echo "${line}" | awk -F ':' '{print $3}')
        if [ -z "${ip_array[counter]}" ] && [ -z "${rpc_port_array[counter]}" ] && [ -z "${p2p_port_array[counter]}" ];then
            ((--counter))
        elif [ -z "${ip_array[counter]}" ] || [  -z "${rpc_port_array[counter]}" ] || [ -z "${p2p_port_array[counter]}" ];then
            LOG_ERROR "Please check ${1} format! e.g:\n127.0.0.1:8250:25500\n127.0.0.1:8251:25501\n127.0.0.2:8252:25502"
            exit 1
        fi
        ((++counter))
    done < "${1}"
}

# shellcheck disable=SC2120
gen_wecross_tars()
{
    certs_dir_prefix=${1}
    for ((i=0;i<counter;i++))
    do
        for((j=0;j<counter;j++))
        do
            # shellcheck disable=SC2057
            if [ ${i} -ne ${j} ];then
                peers_array[i]=${peers_array[i]}"'"${ip_array[j]}:${p2p_port_array[j]}"'",
            fi
        done

        peers_array[i]=$(echo "${peers_array[i]}" | awk '{sub(/.$/,"")}1')
        gen_one_wecross "${certs_dir_prefix}${i}" "${ip_array[i]}" "${rpc_port_array[i]}" "${p2p_port_array[i]}" "${peers_array[i]}"
    done
}

main()
{
    check_params

    if [ ${use_file} -eq 0 ];then
        ip_rpc_p2p=(${ip_param//:/ })
        gen_crt ./ "$router_output"/cert/ 1
        gen_one_wecross "$router_output"/cert/node0 "${ip_rpc_p2p[0]}" "${ip_rpc_p2p[1]}" "${ip_rpc_p2p[2]}"
    elif [ ${use_file} -eq 1 ];then
        parse_ip_file "${ip_file}"
        gen_crt ./ "$router_output"/cert/ ${counter}
        gen_wecross_tars "$router_output"/cert/node
    else
        help
    fi
}

print_result()
{
LOG_INFO "All completed. WeCross routers are generated in: ${router_output}/"
}

# shellcheck disable=SC2068
parse_command $@
check_env
main
print_result
