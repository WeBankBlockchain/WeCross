#!/bin/bash

target_dir=conf/chains
type=''
name=''

help() {
    echo $1
    cat <<EOF
Usage:
    -t <type>                           [Required] type of chain, BCOS2.0 or GM_BCOS2.0 or Fabric1.4
    -n <name>                           [Required] name of chain
    -d <dir>                            [Optional] generated target_directory, default conf/chains/
    -h                                  [Optional] Help
e.g
    bash $0 -t BCOS2.0 -n my_bcos_chain
    bash $0 -t GM_BCOS2.0 -n my_gm_bcos_chain
    bash $0 -t Fabric1.4 -n my_fabric_chain
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
java -cp 'apps/*:lib/*:conf:plugin/*' com.webank.wecross.Generator connection "$type" "${target_dir}/${name}"
