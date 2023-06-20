#!/bin/bash

#set -e

target_dir=conf/accounts
type=''
name=''

help() {
    echo $1
    cat <<EOF
Usage: 
    -t <type>                           [Required] type of account, support: BCOS2.0, GM_BCOS2.0, Fabric1.4, Fabric2.0, BCOS3_ECDSA_EVM, BCOS3_GM_EVM, BCOS3_ECDSA_WASM, BCOS3_GM_WASM
    -n <name>                           [Required] name of account
    -d <dir>                            [Optional] generated target_directory, default conf/accounts/
    -h                                  [Optional] Help
e.g 
    bash $0 -t BCOS2.0 -n my_bcos_account
    bash $0 -t GM_BCOS2.0 -n my_gm_bcos_account
    bash $0 -t BCOS3_ECDSA_EVM -n my_bcos3_account
    bash $0 -t BCOS3_GM_EVM -n my_gm_bcos3_account
    bash $0 -t Fabric1.4 -n my_fabric_account
EOF

    exit 0
}

while getopts "t:n:d:h" option; do
    case $option in
    t) type=$OPTARG ;;
    n) name=$OPTARG ;;
    d) target_dir=$OPTARG ;;
    *) help ;;
    esac
done

[ -z "$type" ] && help
[ -z "$name" ] && help

mkdir -p "${target_dir}/${name}"
java -cp 'apps/*:lib/*:conf:plugin/*' com.webank.wecross.Generator account "$type" "${target_dir}/${name}"
