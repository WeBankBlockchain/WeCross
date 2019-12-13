#!/bin/bash

set -e

BCOS_TYPE=BCOS
JD_TYPE=JD
FABRIC_TYPE=FABRIC
pem_file=
param_arry=
need_help=1
# shellcheck disable=SC2120
help()
{
    echo "$1"
    cat << EOF
Usage:
    -a [Root Dir] [[Stub type] [Stub name]]   Generate stub configuration by lisf of type and name
                                              Supported types: BCOS, FABRIC, JD
    -b [Root Dir] [Stub name]                 Generate BCOS stub configuration
    -f [Root Dir] [Stub name]                 Generate FABRIC stub configuration
    -j [Root Dir] [Stub name]                 Generate JD stub configuration
    -h                                        Call for help
e.g
    bash $0 -d stubs BCOS bcosChain FABRIC fabricChain
EOF
exit 0
}

exec_command()
{
while getopts "a:b:f::jh" option;do
    # shellcheck disable=SC2220
    case ${option} in
    a)
        need_help=0
        check_params "a" $#
        mkdir -p conf/"${2}"
        create_stubs "$*" $#
    ;;
    b)
        need_help=0
        check_params "b" $#
        mkdir -p conf/"${2}"
        create_bcos_stub "${2}" "${3}"
    ;;
    f)
        need_help=0
        check_params "f" $#
        mkdir -p conf/"${2}"
        create_fabric_stub "${2}" "${3}"
    ;;
    j)
        need_help=0
        check_params "j" $#
        mkdir -p conf/"${2}"
        create_jd_stub "${2}" "${3}"
    ;;
    h)  help;;
    esac
done
}

check_params()
{
    # shellcheck disable=SC2170
    if [ "${1}" == "a" ];then
        # shellcheck disable=SC2046
        # shellcheck disable=SC2006
        # shellcheck disable=SC2003
        if [ `expr "${2}" % 2` -ne 0 ];then
            echo -e "\033[31mIllegal number of parameters, -h for help!\033[0m"
            exit 1
        fi

    elif [ "${1}" == "b" ] || [ "${1}" == "f" ] || [ "${1}" == "j" ]; then
        if [ "${2}" -ne 3 ];then
            echo -e "\033[31mIllegal number of parameters, -h for help!\033[0m"
            exit 1
        fi
    fi
}



create_stubs()
{
    # shellcheck disable=SC2206
    param_arry=(${1})
    for ((i=2;i<${2};))
    do
        if [ "${param_arry[i]}" == ${BCOS_TYPE} ];then
            create_bcos_stub "${param_arry[1]}" "${param_arry[i+1]}"
        elif [ "${param_arry[i]}" == ${JD_TYPE} ]; then
            create_jd_stub "${param_arry[1]}" "${param_arry[i+1]}"
        elif [ "${param_arry[i]}" == ${FABRIC_TYPE} ]; then
            create_fabric_stub "${param_arry[1]}" "${param_arry[i+1]}"
        else
            echo -e "\033[31mUnsupported stub type: ${param_arry[i]}\033[0m"
            exit 1
        fi
        i=${i}+2
    done
    echo -e "\033[32mCreate stubs configuration successfully \033[0m"
}

create_bcos_stub()
{
    mkdir -p conf/"${1}"/"${2}"
    gen_bcos_account conf/"${1}"/"${2}"
    cat << EOF > conf/"${1}"/"${2}"/stub.toml
[common]
    stub = '${2}' # stub must be same with directory name
    type = 'BCOS'

[smCrypto]
    # boolean
    enable = false

[account]
    accountFile = 'classpath:/${1}/${2}/${pem_file}'
    password = ''  # if you choose .p12, then password is required


[channelService]
    timeout = 60000  # millisecond
    caCert = 'classpath:/${1}/${2}/ca.crt'
    sslCert = 'classpath:/${1}/${2}/sdk.crt'
    sslKey = 'classpath:/${1}/${2}/sdk.key'
    groupId = 1
    connectionsStr = ['127.0.0.1:20200']

# resources is a list
[[resources]]
    # name cannot be repeated
    name = 'HelloWorldContract'
    type = 'BCOS_CONTRACT'
    contractAddress = '0x8827cca7f0f38b861b62dae6d711efe92a1e3602'
[[resources]]
    name = 'FirstTomlContract'
    type = 'BCOS_CONTRACT'
    contractAddress = '0x584ecb848dd84499639fbe2581bfb8a8774b485c'
EOF

    echo -e "\033[32mCreate ${1}/${2}/stub.toml successfully \033[0m"
}

gen_bcos_account()
{
    expect <<EOF
    spawn bash create_bcos_account.sh
    expect "Password"
    send "123456\r"
    expect "Password"
    send "123456\r"
    expect eof
EOF

    rm accounts/*.public.*
    cd accounts
    # shellcheck disable=SC2035
    # shellcheck disable=SC2012
    pem_file=$(ls *.pem | awk -F'.' '{print $0}')
    cd ..
    cp accounts/"${pem_file}" "${1}"
    rm -rf accounts
}

create_jd_stub()
{
    mkdir -p conf/"${1}"/"${2}"

   cat << EOF > conf/"${1}"/"${2}"/stub.toml
[common]
    stub = '${2}' # stub must be same with directory name
    type = 'JDCHAIN'

# jdServices is a list
[[jdServices]]
     privateKey = '0000000000000000'
     publicKey = '111111111111111'
     password = '222222222222222'
     connectionsStr = '127.0.0.1:18081'
[[jdServices]]
     privateKey = '0000000000000000'
     publicKey = '111111111111111'
     password = '222222222222222'
     connectionsStr = '127.0.0.1:18082'

# resources is a list
[[resources]]
    # name cannot be repeated
    name = 'HelloWorldContract'
    type = 'JDCHAIN_CONTRACT'
    contractAddress = '0x38735ad749aebd9d6e9c7350ae00c28c8903dc7a'
[[resources]]
    name = 'FirstTomlContract'
    type = 'JDCHAIN_CONTRACT'
    contractAddress = '0x38735ad749aebd9d6e9c7350ae00c28c8903dc7a'
EOF

    echo -e "\033[32mCreate ${1}/${2}/stub.toml successfully \033[0m"
}

create_fabric_stub()
{
    mkdir -p conf/"${1}"/"${2}"

   cat << EOF > conf/"${1}"/"${2}"/stub.toml
[common]
    stub = '${2}' # stub must be same with directory name
    type = 'FABRIC'

# fabricServices is a list
[fabricServices]
     channelName = 'mychannel'
     orgName = 'Org1'
     mspId = 'Org1MSP'
     orgUserName = 'Admin'
     orgUserKeyFile = 'classpath:/${1}/${2}/5895923570c12e5a0ba4ff9a908ed10574b475797b1fa838a4a465d6121b8ddf_sk'
     orgUserCertFile = 'classpath:/${1}/${2}/Admin@org1.example.com-cert.pem'
     ordererTlsCaFile = 'classpath:/${1}/${2}/tlsca.example.com-cert.pem'
     ordererAddress = 'grpcs://127.0.0.1:7050'

[peers]
    [peers.a]
        peerTlsCaFile = 'classpath:/${1}/${2}/tlsca.org1.example.com-cert.pem'
        peerAddress = 'grpcs://127.0.0.1:7051'
    [peers.b]
         peerTlsCaFile = 'classpath:/${1}/${2}/tlsca.org2.example.com-cert.pem'
         peerAddress = 'grpcs://127.0.0.1:9051'

# resources is a list
[[resources]]
    # name cannot be repeated
    name = 'HelloWorldContract'
    type = 'FABRIC_CONTRACT'
    chainCodeName = 'mycc'
    chainLanguage = "go"
    peers=['a','b']
[[resources]]
    name = 'FirstTomlContract'
    type = 'FABRIC_CONTRACT'
    chainLanguage = "go"
    chainCodeName = 'cscc'
    peers=['a','b']
EOF

    echo -e "\033[32mCreate ${1}/${2}/stub.toml successfully \033[0m"
}

print_help()
{
    # shellcheck disable=SC2236
    if [ ${need_help} -eq 1 ];then
        help
    fi
}

# shellcheck disable=SC2068
exec_command $@
print_help
