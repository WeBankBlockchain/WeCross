#!/bin/bash

set -e

p12=0
need_help=1
root_dir=
pwd=
pem_file=
p12_file=
stub_name=bcoschain

# shellcheck disable=SC2120
help()
{
    echo "$1"
    cat << EOF
Usage:
    default                             use pem as bcos account type
    -r  [Root Dir]        [Required]    specify the stubs root dir
    -n  [stub name]       [Required]    specify the name of stub
    -p  [password]        [Optional]    password for p12
    -h  Call for help
e.g
    bash $0 -r stubs -n bcoschain -p 123456
EOF
    exit 0
}

parse_params()
{
while getopts "r:p:n:h" option;do
    # shellcheck disable=SC2220
    case ${option} in
    r)
        # shellcheck disable=SC2034
        need_help=0
        root_dir=$OPTARG
    ;;
    p)
        # shellcheck disable=SC2034
        need_help=0
        pwd=$OPTARG
        p12=1
    ;;
    n)
        # shellcheck disable=SC2034
        need_help=0
        stub_name=$OPTARG
    ;;
    h)  help;;
    esac
done
}

gen_bcos_account()
{
    mkdir -p conf/"${root_dir}"/"${stub_name}"
    curl -LO http://raw.githubusercontent.com/FISCO-BCOS/web3sdk/master/tools/get_account.sh
    # shellcheck disable=SC1009
    if [ ${p12} -eq 1 ];then
        expect <<EOF
        spawn bash get_account.sh -p
        expect "Password"
        send "${pwd}\r"
        expect "Password"
        send "${pwd}\r"
        expect eof
EOF
        cd accounts
        # shellcheck disable=SC2035
        # shellcheck disable=SC2012
        p12_file=$(ls *.p12 | awk -F'.' '{print $0}')
        cd ..
        cp accounts/"${p12_file}" conf/"${root_dir}"/"${stub_name}"
    else
        bash get_account.sh
        cd accounts
        # shellcheck disable=SC2035
        # shellcheck disable=SC2012
        pem_file=$(ls *.pem | awk -F'.' '{print $0}')
        cd ..
        cp accounts/"${pem_file}" conf/"${root_dir}"/"${stub_name}"
    fi
    rm -rf accounts
    rm get_account.sh
}

create_bcos_stub()
{
    if [ ${p12} -eq 1 ];then
        account_file=${p12_file}
    else
        account_file=${pem_file}
    fi
    cat << EOF > conf/"${root_dir}"/"${stub_name}"/stub.toml
[common]
    stub = '${stub_name}' # stub must be same with directory name
    type = 'BCOS'

[smCrypto]
    # boolean
    enable = false

[account]
    accountFile = 'classpath:/${root_dir}/${stub_name}/${account_file}'
    password = '${pwd}'


[channelService]
    timeout = 60000  # millisecond
    caCert = 'classpath:/${root_dir}/${stub_name}/ca.crt'
    sslCert = 'classpath:/${root_dir}/${stub_name}/sdk.crt'
    sslKey = 'classpath:/${root_dir}/${stub_name}/sdk.key'
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

    echo -e "\033[32m[INFO] Create ${root_dir}/${stub_name}/stub.toml successfully \033[0m"
}

print_help()
{
    # shellcheck disable=SC2236
    if [ ${need_help} -eq 1 ];then
        help
    fi
}

main()
{
    print_help
    gen_bcos_account
    create_bcos_stub
}

# shellcheck disable=SC2068
parse_params $@
main
