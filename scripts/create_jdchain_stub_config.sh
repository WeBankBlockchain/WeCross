#!/bin/bash

set -e

p12=0
need_help=1
root_dir=stubs
conf_dir=conf
stub_name=jd

# shellcheck disable=SC2120
help()
{
    echo "$1"
    cat << EOF
Usage:
    -n  <stub name>       [Required]    specify the name of stub
    -r  <root dir>        [Optional]    specify the stubs root dir, default is stubs
    -c  <conf path>       [Optional]    specify the path of conf dir, default is conf
    -h  call for help
e.g
    bash $0 -n jd
    bash $0 -n jd -r stubs
    bash $0 -n jd -r stubs -c conf
EOF
    exit 0
}

parse_params()
{
while getopts "r:n:c:h" option;do
    # shellcheck disable=SC2220
    case ${option} in
    r)
        # shellcheck disable=SC2034
        need_help=0
        root_dir=$OPTARG
    ;;
    n)
        # shellcheck disable=SC2034
        need_help=0
        stub_name=$OPTARG
    ;;
    c)
        # shellcheck disable=SC2034
        need_help=0
        conf_dir=$OPTARG
    ;;
    h)  help;;
    esac
done
}

create_jd_stub()
{
    mkdir -p "${conf_dir}"/"${root_dir}"/"${stub_name}"

   cat << EOF > "${conf_dir}"/"${root_dir}"/"${stub_name}"/stub.toml
[common]
    stub = '${stub_name}' # stub must be same with directory name
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
    # name must be unique
    name = 'HelloWorldContract'
    type = 'JDCHAIN_CONTRACT'
    contractAddress = ''
[[resources]]
    name = 'FirstTomlContract'
    type = 'JDCHAIN_CONTRACT'
    contractAddress = ''
EOF

    echo -e "\033[32m[INFO] Create ${conf_dir}/${root_dir}/${stub_name}/stub.toml successfully \033[0m"
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
    create_jd_stub
}

# shellcheck disable=SC2068
parse_params $@
main
