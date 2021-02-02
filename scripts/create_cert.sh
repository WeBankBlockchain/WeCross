#!/bin/bash

#set -e

ca_cert_dir=$(pwd)
target_dir=$(pwd)
node_count=
cert_conf='cert.cnf'
generate_ca=''
generate_rsa_cert='true'

help() {
    echo $1
    cat <<EOF
Usage: 
    -c                                  [Optional] generate ca certificate
    -C <number>                         [Optional] the number of node certificate generated, work with '-n' opt, default: 1
    -D <dir>                            [Optional] the ca certificate directory, work with '-n', default: './'
    -d <dir>                            [Required] generated target_directory
    -e                                  [optional] generate ecc node cert, default rsa node cert
    -n                                  [Optional] generate node certificate
    -t                                  [Optional] cert.cnf path, default: cert.cnf
    -h                                  [Optional] Help
e.g 
    bash $0 -c -d ./ca
    bash $0 -n -D ./ca -d ./ca/node
    bash $0 -n -D ./ca -d ./ca/node -C 10
EOF

    exit 0
}

LOG_WARN() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_FALT() {
    local content=${1}
    echo -e "\033[31m[FALT] ${content}\033[0m"
    exit 1
}

check_env() {
    # shellcheck disable=SC2143
    # shellcheck disable=SC2236
    [ ! -z "$(openssl version | grep 1.0.2)" ] || [ ! -z "$(openssl version | grep 1.1)" ] || [ ! -z "$(openssl version | grep reSSL)" ] || {
        LOG_ERROR "Please install openssl!"
        #echo "download openssl from https://www.openssl.org."
        LOG_INFO "Use \"openssl version\" command to check."
        exit 1
    }
    if [ ! -z "$(openssl version | grep reSSL)" ]; then
        export PATH="/usr/local/opt/openssl/bin:$PATH"
    fi
    if [ "$(uname)" == "Darwin" ]; then
        macOS="macOS"
    fi
    if [ "$(uname -m)" != "x86_64" ]; then
        x86_64_arch="false"
    fi
}

check_name() {
    local name="$1"
    local value="$2"
    [[ "$value" =~ ^[a-zA-Z0-9._-]+$ ]] || {
        LOG_FALT "$name name [$value] invalid, it should match regex: ^[a-zA-Z0-9._-]+\$"
    }
}

file_must_exists() {
    if [ ! -f "$1" ]; then
        LOG_FALT "$1 file does not exist, please check!"
    fi
}

file_must_not_exists() {
    if [ -f "$1" ]; then
        LOG_FALT "$1 file exists, please check!"
    fi
}

dir_must_exists() {
    if [ ! -d "$1" ]; then
        LOG_FALT "$1 DIR does not exist, please check!"
    fi
}

dir_must_not_exists() {
    if [ -e "$1" ]; then
        LOG_FALT "$1 DIR exists, please clean old DIR!"
    fi
}

generate_cert_conf() {
    local output=$1
    cat <<EOF >${output}
[ca]
default_ca=default_ca
[default_ca]
default_days = 3650
default_md = sha256

[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
[req_distinguished_name]
countryName = CN
countryName_default = CN
stateOrProvinceName = State or Province Name (full name)
stateOrProvinceName_default =GuangDong
localityName = Locality Name (eg, city)
localityName_default = ShenZhen
organizationalUnitName = Organizational Unit Name (eg, section)
organizationalUnitName_default = WeCross
commonName =  Organizational  commonName (eg, WeCross)
commonName_default = WeCross
commonName_max = 64

[ v3_req ]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment

[ v4_req ]
basicConstraints = CA:TRUE

EOF
}

gen_chain_cert() {

    if [ ! -e "${cert_conf}" ]; then
        generate_cert_conf 'cert.cnf'
    else
        cp "${cert_conf}" . 2>/dev/null
    fi

    local chaindir="${1}"

    file_must_not_exists $chaindir/ca.key
    file_must_not_exists $chaindir/ca.crt
    file_must_exists 'cert.cnf'

    mkdir -p $chaindir

    openssl genrsa -out $chaindir/ca.key 4096
    openssl req -new -x509 -days 3650 -subj "/CN=WeCross/O=WeCross/OU=chain" -key $chaindir/ca.key -out $chaindir/ca.crt
    cp "cert.cnf" $chaindir 2>/dev/null

    LOG_INFO "Build ca cert successful!"
}

gen_cert_secp256k1() {
    capath="$1"
    certpath="$2"
    type="$3"
    openssl ecparam -out $certpath/${type}.param -name secp256k1
    openssl genpkey -paramfile $certpath/${type}.param -out $certpath/${type}.key
    openssl pkey -in $certpath/${type}.key -pubout -out $certpath/${type}.pubkey
    openssl req -new -sha256 -subj "/CN=WeCross/O=WeCross/OU=${type}" -key $certpath/${type}.key -config $capath/cert.cnf -out $certpath/${type}.csr
    openssl x509 -req -days 3650 -sha256 -in $certpath/${type}.csr -CAkey $capath/ca.key -CA $capath/ca.crt \
        -force_pubkey $certpath/${type}.pubkey -out $certpath/${type}.crt -CAcreateserial -extensions v3_req -extfile $capath/cert.cnf
    # openssl ec -in $certpath/${type}.key -outform DER | tail -c +8 | head -c 32 | xxd -p -c 32 | cat >$certpath/${type}.private
    cat ${capath}/ca.crt >>$certpath/${type}.crt
    rm -f $certpath/${type}.csr $certpath/${type}.pubkey $certpath/${type}.param
}

gen_ecc_node_cert() {
    if [ "" == "$(openssl ecparam -list_curves 2>&1 | grep secp256k1)" ]; then
        LOG_FALT "Openssl don't support secp256k1, please upgrade openssl!"
    fi

    local capath="${1}"
    local ndpath="${2}"
    local node="${3}"

    LOG_INFO "Generate key for ${node}"

    dir_must_exists "$capath"
    file_must_exists "$capath/ca.key"
    file_must_not_exists "$ndpath/ssl.key"
    file_must_not_exists "$ndpath/ssl.crt"

    check_name ssl "$node"

    mkdir -p $ndpath

    gen_cert_secp256k1 "$capath" "$ndpath" "ssl"
    #nodeid is pubkey
    openssl ec -in $ndpath/ssl.key -text -noout | sed -n '7,11p' | tr -d ": \n" | awk '{print substr($0,3);}' | cat >$ndpath/node.nodeid
    # openssl x509 -serial -noout -in $ndpath/ssl.crt | awk -F= '{print $2}' | cat >$ndpath/node.serial
    cp $capath/ca.crt $ndpath
    # cd $ndpath

    LOG_INFO "Build ecc ${node} cert successful!"
}

gen_rsa_node_cert() {
    local capath="${1}"
    local ndpath="${2}"
    local node="${3}"

    dir_must_exists "$capath"
    file_must_exists "$capath/ca.key"
    check_name node "$node"

    file_must_not_exists "$ndpath"/ssl.key
    file_must_not_exists "$ndpath"/ssl.crt
    mkdir -p $ndpath

    openssl genrsa -out $ndpath/ssl.key 4096
    openssl req -new -sha256 -subj "/CN=FISCO-BCOS/O=fisco-bcos/OU=agency" -key $ndpath/ssl.key -config $capath/cert.cnf -out $ndpath/ssl.csr
    openssl x509 -req -days 3650 -sha256 -CA $capath/ca.crt -CAkey $capath/ca.key -CAcreateserial \
        -in $ndpath/ssl.csr -out $ndpath/ssl.crt -extensions v4_req -extfile $capath/cert.cnf

    openssl pkcs8 -topk8 -in "$ndpath"/ssl.key -out "$ndpath"/pkcs8_ssl.key -nocrypt
    cp $capath/ca.crt $capath/cert.cnf $ndpath/

    rm -f $ndpath/ssl.csr
    rm -f $ndpath/ssl.key

    mv "$ndpath"/pkcs8_ssl.key "$ndpath"/ssl.key

    LOG_INFO "Build rsa ${node} cert successful!"
}

gen_node_cert() {
    if [[ ${generate_rsa_cert} == 'true' ]]; then
        gen_rsa_node_cert "$1" "$2" "$3" 2>&1
    else
        gen_ecc_node_cert "$1" "$2" "$3" 2>&1
    fi
}

gen_all_node_cert() {
    local ca_dir=$1
    local target_dir=$2
    mkdir -p ${target_dir}

    for ((i = 0; i < ${node_count}; ++i)); do
        {
            gen_node_cert "${ca_dir}" "${target_dir}/node${i}" "node${i}"
        }
    done
}

while getopts "cC:d:eD:nt:h" option; do
    case $option in
    c) generate_ca='true' ;;
    C) node_count=$OPTARG ;;
    d) target_dir=$OPTARG ;;
    D) ca_cert_dir=$OPTARG ;;
    e) generate_rsa_cert='false' ;;
    n) generate_ca='false' ;;
    t) cert_conf=$OPTARG ;;
    *) help ;;
    esac
done

main() {
    if [[ ${generate_ca} == 'true' ]]; then
        gen_chain_cert "${target_dir}" 2>&1
    elif [[ ${generate_ca} == 'false' ]]; then
        if [[ -z "$node_count" ]]; then
            gen_node_cert "${ca_cert_dir}" "${target_dir}" "node" 2>&1
        else
            gen_all_node_cert "${ca_cert_dir}" "${target_dir}" 2>&1
        fi
    else
        help
    fi
}

check_env
main
