#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}

type=''
deploy='true'
chain=''
contract=''
SECURIY_FILE='./.wecross.security'

help() {
  echo $1
  cat <<EOF
Usage:
    -c <chain name>                     [Required] chain name
    -u <upgrade>                        [Optional] upgrade proxy/hub contract if proxy/hub contract has been deployed, default deploy proxy/hub contract
    -t <type>                           [Required] type of chain, support: BCOS2.0, GM_BCOS2.0, Fabric1.4, Fabric2.0, BCOS3_ECDSA_EVM, BCOS3_GM_EVM
    -P <proxy contract>                 [Optional] upgrade/deploy operation on proxy contract
    -H <hub contract>                   [Optional] upgrade/deploy operation on hub contract
    -h                                  [Optional] Help
e.g
    bash $0 -t BCOS2.0    -c chains/bcos -P
    bash $0 -t BCOS2.0    -c chains/bcos -H
    bash $0 -t BCOS2.0    -c chains/bcos -u -P
    bash $0 -t BCOS2.0    -c chains/bcos -u -H
    bash $0 -t BCOS3_ECDSA_EVM    -c chains/bcos3 -P
    bash $0 -t BCOS3_ECDSA_EVM    -c chains/bcos3 -H
    bash $0 -t BCOS3_ECDSA_EVM    -c chains/bcos3 -u -P
    bash $0 -t BCOS3_ECDSA_EVM    -c chains/bcos3 -u -H
    bash $0 -t Fabric1.4  -c chains/fabric -P
    bash $0 -t Fabric1.4  -c chains/fabric -H
    bash $0 -t Fabric1.4  -c chains/fabric -u -P
    bash $0 -t Fabric1.4  -c chains/fabric -u -H
    bash $0 -t Fabric2.0  -c chains/fabric2 -P
    bash $0 -t Fabric2.0  -c chains/fabric2 -H
    bash $0 -t Fabric2.0  -c chains/fabric2 -u -P
    bash $0 -t Fabric2.0  -c chains/fabric2 -u -H
EOF

  exit 0
}

LOG_INFO() {
  local content=${1}
  echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
  local content=${1}
  echo -e "\033[31m[ERROR] ${content}\033[0m"
}

create_jvm_security() {
  if [[ ! -f ${SECURIY_FILE} ]]; then
    echo "jdk.disabled.namedCurves = " >${SECURIY_FILE}
    # LOG_INFO "create new file ${SECURIY_FILE}"
  fi
}

bcos_proxy_contract() {
  local chainName="$1"
  local deploy="$2"
  local type="$3"
  local packageName=""
  case $type in
  "BCOS2.0")
    packageName="bcos.normal"
    ;;
  "GM_BCOS2.0")
    packageName="bcos.guomi"
    ;;
  "BCOS3_ECDSA_EVM" | "BCOS3_GM_EVM")
    packageName="bcos3"
    ;;
  esac

  local op="deploy"
  if [[ "${deploy}" == "false" ]]; then
    op="upgrade"
  fi
  java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub."${packageName}".preparation.ProxyContractDeployment "${op}" "${chainName}"
}

bcos_hub_contract() {
  local chainName="$1"
  local deploy="$2"
  local type="$3"
  local packageName=""
  case $type in
  "BCOS2.0")
    packageName="bcos.normal"
    ;;
  "GM_BCOS2.0")
    packageName="bcos.guomi"
    ;;
  "BCOS3_ECDSA_EVM" | "BCOS3_GM_EVM")
    packageName="bcos3"
    ;;
  esac

  local op="deploy"
  if [[ "${deploy}" == "false" ]]; then
    op="upgrade"
  fi
  java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub."${packageName}".preparation.HubContractDeployment "${op}" "${chainName}"
}

deploy_bcos_proxy_contract() {
  local chainName="$1"
  local isGM="$2"
  if [[ "$isGM" == "true" ]]; then
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.preparation.ProxyContractDeployment deploy "${chainName}"
  else
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.ProxyContractDeployment deploy "${chainName}"
  fi
}

deploy_bcos_hub_contract() {
  local chainName="$1"
  local isGM="$2"
  if [[ "$isGM" == "true" ]]; then
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.preparation.HubContractDeployment deploy "${chainName}"
  else
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.HubContractDeployment deploy "${chainName}"
  fi
}

update_bcos_proxy_contract() {
  local chainName="$1"
  local isGM="$2"
  if [[ "$isGM" == "true" ]]; then
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.ProxyContractDeployment upgrade "${chainName}"
  else
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.preparation.ProxyContractDeployment upgrade "${chainName}"
  fi
}

update_bcos_hub_contract() {
  local chainName="$1"
  local isGM="$2"
  if [[ "$isGM" == "true" ]]; then
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.HubContractDeployment upgrade "${chainName}"
  else
    java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.preparation.HubContractDeployment upgrade "${chainName}"
  fi
}

deploy_fabric_proxy_contract() {
  local chainName="$1"
  java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric.proxy.ProxyChaincodeDeployment deploy "${chainName}"
}

deploy_fabric_hub_contract() {
  local chainName="$1"
  java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric.hub.HubChaincodeDeployment deploy "${chainName}"
}

update_fabric_proxy_contract() {
  local chainName="$1"
  java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric.proxy.ProxyChaincodeDeployment upgrade "${chainName}"
}

update_fabric_hub_contract() {
  local chainName="$1"
  java -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric.hub.HubChaincodeDeployment upgrade "${chainName}"
}

deploy_fabric2_proxy_contract() {
  local chainName="$1"
  java -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric2.proxy.ProxyChaincodeDeployment deploy "${chainName}"
}

deploy_fabric2_hub_contract() {
  local chainName="$1"
  java -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric2.hub.HubChaincodeDeployment deploy "${chainName}"
}

update_fabric2_proxy_contract() {
  local chainName="$1"
  java -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric2.proxy.ProxyChaincodeDeployment upgrade "${chainName}"
}

update_fabric2_hub_contract() {
  local chainName="$1"
  java -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative=false -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448" -cp conf/:lib/*:plugin/* com.webank.wecross.stub.fabric2.hub.HubChaincodeDeployment upgrade "${chainName}"
}

main() {
  local type="$1"
  local chain="$2"
  local deploy="$3"
  local contract="$4"

  [ -z "$type" ] && help
  [ -z "$chain" ] && help
  [ -z "$deploy" ] && help
  [ -z "$contract" ] && help

  create_jvm_security

  LOG_INFO " deploy_system_contract, type: ${type}, chain: ${chain}, deploy: ${deploy}, contract: ${contract}"

  case $type in
  "BCOS2.0" | "BCOS3_ECDSA_EVM" | "GM_BCOS2.0" | "BCOS3_GM_EVM")
    if [[ "${contract}" == "proxy" ]]; then
      bcos_proxy_contract "${chain}" "${deploy}" "${type}"
    elif [[ "${contract}" == "hub" ]]; then
      bcos_hub_contract "${chain}" "${deploy}" "${type}"
    fi
    ;;
  "Fabric1.4")
    if [[ "${deploy}" == "true" ]] && [[ "${contract}" == "proxy" ]]; then
      deploy_fabric_proxy_contract "${chain}"
    elif [[ "${deploy}" == "true" ]] && [[ "${contract}" == "hub" ]]; then
      deploy_fabric_hub_contract "${chain}"
    elif [[ "${deploy}" == "false" ]] && [[ "${contract}" == "proxy" ]]; then
      update_fabric_proxy_contract "${chain}"
    elif [[ "${deploy}" == "false" ]] && [[ "${contract}" == "hub" ]]; then
      update_fabric_hub_contract "${chain}"
    fi
    ;;
  "Fabric2.0")
    if [[ "${deploy}" == "true" ]] && [[ "${contract}" == "proxy" ]]; then
      deploy_fabric2_proxy_contract "${chain}"
    elif [[ "${deploy}" == "true" ]] && [[ "${contract}" == "hub" ]]; then
      deploy_fabric2_hub_contract "${chain}"
    elif [[ "${deploy}" == "false" ]] && [[ "${contract}" == "proxy" ]]; then
      update_fabric2_proxy_contract "${chain}"
    elif [[ "${deploy}" == "false" ]] && [[ "${contract}" == "hub" ]]; then
      update_fabric2_hub_contract "${chain}"
    fi
    ;;
  *)
    echo "Unrecognized type" && help
    ;;
  esac
}

while getopts "c:ut:hHP" option; do
  case $option in
  c) chain=$OPTARG ;;
  u) deploy='false' ;;
  t) type=$OPTARG ;;
  H) contract='hub' ;;
  P) contract='proxy' ;;
  *) help ;;
  esac
done

[ -z "$type" ] && {
  LOG_ERROR "Unrecognized type"
  exit
}
[ -z "$chain" ] && {
  LOG_ERROR "Unrecognized chain"
  exit
}
[ -z "$contract" ] && {
  LOG_ERROR "Unrecognized contract type"
  exit
}
main "${type}" "${chain}" "${deploy}" "${contract}"
