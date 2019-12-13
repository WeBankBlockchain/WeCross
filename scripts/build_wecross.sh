#!/bin/bash

set -e

counter=0
network=
ip_array=
rpc_port_array=
p2p_port_array=
peers_array=
need_help=1

# shellcheck disable=SC2120
help()
{
    echo "$1"
    cat << EOF
Usage:
    -i [Network ID] [IP] [Port] [Port]     Init wecross project by wecross network id, ip, rpc_port and p2p_port, e.g: payment 127.0.0.1 8250 25500
    -f [Network ID] [File]                 Init wecross project by wecross network id and ip&ports file. file should be splited by line "ip rpc_port p2p_port" e.g: 127.0.0.1 8250 25500
    -h                                     Call for help
e.g
    bash $0 -i payment 127.0.0.1 8250 25500
EOF
exit 0
}

check_env() {
    # shellcheck disable=SC2143
    # shellcheck disable=SC2236
    [ ! -z "$(openssl version | grep 1.0.2)" ] || [ ! -z "$(openssl version | grep 1.1)" ] || [ ! -z "$(openssl version | grep reSSL)" ] || {
        echo "please install openssl!"
        #echo "download openssl from https://www.openssl.org."
        echo "use \"openssl version\" command to check."
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

exec_command()
{
while getopts "i:f:h" option;do
    # shellcheck disable=SC2220
    case ${option} in
    i)
        check_params "i" $#
        need_help=0
        network="${2}"
        build_project
        gen_crt 1
        gen_one_wecross 0 "${3}" "${4}" "${5}"
        finish_jod
    ;;
    f)
        check_params "f" $#
        need_help=0
        network="${2}"
        parse_ip_file "${3}"
        build_project
        gen_wecross_tars
        finish_jod
    ;;
    h)  help;;
    esac
done
}

check_params()
{
    # shellcheck disable=SC2170
    if [ "${1}" == "i" ];then
        if [ "${2}" -ne 5 ];then
            echo -e "\033[31mIllegal number of parameters, -h for help!\033[0m"
            exit 1
        fi
    elif [ "${1}" == "f" ];then
        if [ "${2}" -ne 3 ];then
            echo -e "\033[31mIllegal number of parameters, -h for help!\033[0m"
            exit 1
        fi
    fi
}

build_project()
{
    if [ -d WeCross ];then
        echo -e "\033[31mWeCross: File exists, please change directory to run this script\033[0m"
        exit 1
    fi
    git clone https://github.com/WeBankFinTech/WeCross.git
    cd WeCross
    ./gradlew assemble 2>&1 | tee output.log
    # shellcheck disable=SC2046
    # shellcheck disable=SC2006
    if [ `grep -c "BUILD SUCCESSFUL" output.log` -eq '0' ]; then
        echo -e "\033[31mBuild Wecross project failed \033[0m"
        echo -e "\033[31mSee output.log for details \033[0m"
        mv output.log ../output.log
        cd ..
        exit 1
    fi
    cd ..
}

gen_crt()
{
    cd WeCross
    # get ca.crt
    bash scripts/create_cert.sh -c
    # get node.crt by number
    bash scripts/create_cert.sh -n -C "${1}"
    cd ..
}

#index ip rpc_port p2p_port pers
gen_one_wecross()
{
    cd WeCross
    output=${2}-${3}-${4}
    cp -r dist "${output}"
    cp -r node"${1}" "${output}"/conf/p2p
    gen_conf "${output}"/conf/wecross.toml "${2}" "${3}" "${4}" "${5}"
    cp -r "${output}" ../
    cd ..
}

gen_one_wecross_tar()
{
    cd WeCross
    output=${2}-${3}-${4}
    cp -r dist "${output}"
    cp -r node"${1}" "${output}"/conf/p2p
    gen_conf "${output}"/conf/wecross.toml "${2}" "${3}" "${4}" "${5}"
    tar -czf ../"${output}".tar.gz "${output}"
    cd ..
    echo -e "\033[32mCreate ${output}.tar.gz  successfully \033[0m"
}

gen_conf()
{
    cat << EOF > "${1}"
[common]
    network = '${network}'
    visible = true

[stubs]
    path = 'classpath:stubs'

[server] # tomcat server
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
    enableTestResource = true
EOF
}

parse_ip_file()
{
    # shellcheck disable=SC2162
    while read line;do
        ip_array[counter]=$(echo "${line}" | awk '{print $1}')
        rpc_port_array[counter]=$(echo "${line}" | awk '{print $2}')
        p2p_port_array[counter]=$(echo "${line}" | awk '{print $3}')
        if [ -z "${ip_array[counter]}" -o -z "${rpc_port_array[counter]}" -o -z "${p2p_port_array[counter]}" ];then
            echo -e "\033[31mPlease check ${1}, make sure there is no empty line!\033[0m"
            exit 1
        fi
        ((++counter))
    done < "${1}"
}

# shellcheck disable=SC2120
gen_wecross_tars()
{
    gen_crt "${counter}"

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
        gen_one_wecross_tar "${i}" "${ip_array[i]}" "${rpc_port_array[i]}" "${p2p_port_array[i]}" "${peers_array[i]}"
    done
}

finish_jod()
{
    if [ -d WeCross ];then
        rm -rf WeCross
    fi
    echo -e "\033[32mBuild Wecross successfully \033[0m"
}

print_help()
{
    # shellcheck disable=SC2236
    if [ ${need_help} -eq 1 ];then
        help
    fi
}

check_env
# shellcheck disable=SC2068
exec_command $@
print_help
