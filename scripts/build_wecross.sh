#!/bin/bash

set -e
LANG=en_US.utf8

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
plugins='BCOS2.0,Fabric1.4'
deps_dir='./plugin/'

bcos_stub_jar_name=bcos-stub.jar
bcos_stub_url='https://oss.sonatype.org/service/local/repositories/snapshots/content/com/webank/wecross-bcos-stub/1.0.0-rc2-0414-SNAPSHOT/wecross-bcos-stub-1.0.0-rc2-0414-20200414.030542-1-all.jar'
bcos_stub_md5='99d08a92b7c5ccf79362ec0caec4eb01'

fabric_stub_jar_name=fabric-stub.jar
fabric_stub_url='https://oss.sonatype.org/service/local/repositories/snapshots/content/com/webank/wecross-fabric-stub/1.0.0-rc2-0414-SNAPSHOT/wecross-fabric-stub-1.0.0-rc2-0414-20200414.032736-1-all.jar'
fabric_stub_md5='0fb7e1ceca0996ef88a1203ea232b058'

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
    -n  <zone id>                   [Required]   set zone ID
    -l  <ip:rpc-port:p2p-port>      [Optional]   "ip:rpc-port:p2p-port" e.g:"127.0.0.1:8250:25500"
    -f  <ip list file>              [Optional]   split by line, every line should be "ip:rpc-port:p2p-port". eg "127.0.0.1:8250:25500"
    -c  <ca dir>                    [Optional]   dir of existing ca
    -o  <output dir>                [Optional]   default ./${router_output}/
    -z  <generate tar packet>       [Optional]   default no
    -T  <enable test mode>          [Optional]   default no. Enable test resource.
    -p  <enable plugin>             [Optional]   enabled plugins, split by ',', e.g: BCOS2.0,Fabric1.4, default enable all plugins
    -d  <dependencies dir>          [Optional]   dependencies dir, default './deps'
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
while getopts "o:n:l:f:c:d:p:zTh" option;do
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
    p)
        plugins=$OPTARG
    ;;
    d)
        deps_dir=$OPTARG
    ;;
    h)  help;;
    esac
done
}

check_params()
{
    if [ -z "${zone}" ];then
        LOG_ERROR "Please set [zone id]"
        help
        exit 1
    fi

    if [ -z "${use_file}" ];then
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

gen_crt()
{
    scripts_dir=${1}/
    output=${2}/
    num=${3}

    # generate ca.crt
    if [ ${ca} -eq 0 ];then
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

download_bcos_plugin()
{
    mkdir -p ${deps_dir}
    [ -f "${deps_dir}/${bcos_stub_jar_name}" ] && [ ${bcos_stub_md5} != $(md5sum "${deps_dir}/${bcos_stub_jar_name}" | cut -f1 -d' ') ] && rm "${deps_dir}/${bcos_stub_jar_name}"
    [ ! -f "${deps_dir}/${bcos_stub_jar_name}" ] && wget -O "${deps_dir}/${bcos_stub_jar_name}" "$bcos_stub_url"
    [ $bcos_stub_md5 != $(md5sum "${deps_dir}/${bcos_stub_jar_name}" | cut -f1 -d' ') ] && {
        LOG_ERROR "Download bcos-stub failed!"
        exit 1
    }
    LOG_INFO "Download ${bcos_stub_jar_name} success!"
}

download_fabric_plugin()
{
    mkdir -p ${deps_dir}
    #download and initialize the plugin
    [ -f "${deps_dir}/${fabric_stub_jar_name}" ] && [ $fabric_stub_md5 != $(md5sum "${deps_dir}/${fabric_stub_jar_name}" | cut -f1 -d' ') ] && rm "${deps_dir}/${fabric_stub_jar_name}"
    [ ! -f "${deps_dir}/${fabric_stub_jar_name}" ] && wget -O "${deps_dir}/${fabric_stub_jar_name}" "$fabric_stub_url"
    [ $fabric_stub_md5 != $(md5sum "${deps_dir}/${fabric_stub_jar_name}" | cut -f1 -d' ') ] && {
        LOG_ERROR "Download fabirc-stub failed!"
        exit 1
    }
    LOG_INFO "Download ${fabric_stub_jar_name} success!"
}

download_plugins()
{
    to_dir=${1}

    echo "plugins: $plugins"
    mkdir -p ${to_dir}
    mkdir -p ${deps_dir}
    #download plugins
    plugins_array=(${plugins/,/ })
    for plugin in ${plugins_array[*]};do
        echo ${plugin}
        case ${plugin} in
        BCOS2.0)
            download_bcos_plugin
            cp ${deps_dir}/${bcos_stub_jar_name} ${to_dir}/${bcos_stub_jar_name}
            ;;
        Fabric1.4)
        	download_fabric_plugin
        	cp ${deps_dir}/${fabric_stub_jar_name} ${to_dir}/${fabric_stub_jar_name}
            ;;

        *)
            LOG_ERROR "Unknown plugin: ${plugin}"
            exit 1
            ;;
        esac
    done
}

#index ip rpc_port p2p_port peers
gen_one_wecross()
{
    #default execute dir: ../WeCross
    cert_dir=${1}
    output=${router_output}/${2}-${3}-${4}
    target=${2}-${3}-${4}

    # mkdir
    mkdir -p ${output}/
    mkdir -p ${output}/conf/accounts
    mkdir -p ${output}/conf/chains
    mkdir -p ${output}/plugin

    # copy files
    chmod u+x ${wecross_dir}./*.sh
    cp -r ${wecross_dir}./*.sh "${output}/"
    cp -r ${wecross_dir}/apps "${output}/"
    cp -r ${wecross_dir}/lib "${output}/"

    # download plugin
    download_plugins ${output}/plugin

    cp -r "${wecross_dir}/conf" "${output}/"
    cp -r "${cert_dir}"/* "${output}"/conf/
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
    zone = '${zone}'
    visible = true

[chains]
    path = 'classpath:chains'

[rpc] # rpc ip & port
    address = '${2}'
    port = ${3}
    caCert = 'classpath:ca.crt'
    sslCert = 'classpath:ssl.crt'
    sslKey = 'classpath:ssl.key'

[p2p]
    listenIP = '0.0.0.0'
    listenPort = ${4}
    caCert = 'classpath:ca.crt'
    sslCert = 'classpath:ssl.crt'
    sslKey = 'classpath:ssl.key'
    peers = [${5}]
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
        gen_crt ${wecross_dir} "$router_output"/cert/ 1
        gen_one_wecross "$router_output"/cert/node0 "${ip_rpc_p2p[0]}" "${ip_rpc_p2p[1]}" "${ip_rpc_p2p[2]}"
    elif [ ${use_file} -eq 1 ];then
        parse_ip_file "${ip_file}"
        gen_crt ${wecross_dir} "$router_output"/cert/ ${counter}
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