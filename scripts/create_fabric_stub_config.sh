#!/bin/bash

set -e

p12=0
need_help=1
root_dir=stubs
conf_dir=conf
stub_name=fabric

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
    bash $0 -n fabric
    bash $0 -n fabric -r stubs
    bash $0 -n fabric -r stubs -c conf
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

create_fabric_stub()
{
    mkdir -p "${conf_dir}"/"${root_dir}"/"${stub_name}"

    cat << EOF > "${conf_dir}"/"${root_dir}"/"${stub_name}"/stub.toml
[common]
    stub = '${stub_name}' # stub must be same with directory name
    type = 'FABRIC'

# fabricServices is a list
[fabricServices]
    channelName = 'mychannel'
    orgName = 'Org1'
    mspId = 'Org1MSP'
    orgUserName = 'Admin'
    orgUserKeyFile = 'classpath:/${root_dir}/${stub_name}/orgUserKeyFile'
    orgUserCertFile = 'classpath:/${root_dir}/${stub_name}/orgUserCertFile'
    ordererTlsCaFile = 'classpath:/${root_dir}/${stub_name}/ordererTlsCaFile'
    ordererAddress = 'grpcs://127.0.0.1:7050'

[peers]
    [peers.org1]
        peerTlsCaFile = 'classpath:/${root_dir}/${stub_name}/peerOrg1CertFile'
        peerAddress = 'grpcs://127.0.0.1:7051'
    [peers.org2]
        peerTlsCaFile = 'classpath:/${root_dir}/${stub_name}/peerOrg2CertFile'
        peerAddress = 'grpcs://127.0.0.1:9051'

# resources is a list
[[resources]]
    # name must be unique
    name = 'HelloWeCross'
    type = 'FABRIC_CONTRACT'
    chainCodeName = 'mycc'
    chainLanguage = "go"
    peers=['org1','org2']
[[resources]]
    name = 'HelloWorld'
    type = 'FABRIC_CONTRACT'
    chainCodeName = 'mygg'
    chainLanguage = "go"
    peers=['org1','org2']
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
    create_fabric_stub
}

# shellcheck disable=SC2068
parse_params $@
main
