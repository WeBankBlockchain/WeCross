#!/bin/bash

#set -e

target_dir=conf/accounts
type=''
name=''

help() {
    echo $1
    cat << EOF
Usage: 
<<<<<<< HEAD
    -t <type>                           [Required] type of account, BCOS2.0 or Fabric1.4
    -n <name>                           [Required] name of account
=======
    -t                                  [Required] type of account, BCOS2.0 or Fabric1.4
    -n                                  [Required] name of account
>>>>>>> upstream/release-rc2
    -d <dir>                            [Optional] generated target_directory, default conf/accounts/
    -h                                  [Optional] Help
e.g 
    bash $0 -t BCOS2.0 -n my_bcos_account
    bash $0 -t Fabric1.4 -n my_fabric_account
EOF

exit 0
}

while getopts "t:n:d:h" option;do
    case $option in
    t) type=$OPTARG ;;
    n) name=$OPTARG ;;
    d) target_dir=$OPTARG ;;
    *) help;;
    esac
done

[ -z "$type" ] && help
[ -z "$name" ] && help

mkdir -p "${target_dir}/${name}"
java -cp 'apps/*:lib/*:conf:plugin/*' com.webank.wecross.Generator account "$type" "${target_dir}/${name}"