#!/bin/bash
dirPath="$(cd "$(dirname "$0")" && pwd)"
cd "${dirPath}" || exit 1

# rpc ip
rpc_ip=""
# rpc 端口
rpc_port=""
#
disk_dir="."

# 磁盘容量告警阈值，默认剩余5%开始告警
disk_space_threshold=95
memory_free_threshold=5

alarm() {
  echo "$1"
  alert_msg="$1"
  alert_ip=$(/sbin/ifconfig eth0 | grep inet | grep -v inet6 | awk '{print $2}')
  alert_time=$(date "+%Y-%m-%d %H:%M:%S")

  # TODO: alarm the message, mail or phone

   echo "[${alert_time}]:[${alert_ip}]:${alert_msg}"
#   | mail -s "fisco-bcos alarm message" 123456@me.com
}

# echo message with time
info() {
  time=$(date "+%Y-%m-%d %H:%M:%S")
  echo "[$time] $1"
}

error() {
  echo -e "\033[31m $1 \033[0m"
}

dir_must_exists() {
  if [ ! -d "$1" ]; then
    error "$1 DIR does not exist, please check!"
    exit 1
  fi
}

function check_disk() {
  local dir="$1"
  local disk_space_usage_percent
  disk_space_usage_percent=$(df -h "${dir}" | grep /dev | awk -F" " '{print $5}' | cut -d"%" -f1)
  # info "disk_space_left_percent: ${disk_space_left_percent}, disk_space_threshold:${disk_space_threshold}"
  if [[ ${disk_space_threshold} -lt ${disk_space_usage_percent} ]]; then
    alarm " ERROR! insufficient disk capacity, monitor disk directory: ${dir}, used disk space percent: ${disk_space_usage_percent}%"
    return 1
  fi
}

# check memory
function check_memory() {
  local memory_free
  memory_free=$(free -g | grep Mem | awk '{print $4}')
  if [[ ${memory_free_threshold} -gt ${memory_free} ]]; then
    alarm " ERROR! insufficient memory, free memory: ${memory_free}G"
    return 1
  fi
}

# check if nodeX is work well
function check_router_work_properly() {
  local config_ip="${1}"
  local config_port="${2}"

  local testResult
  testResult=$(curl -s "http://$config_ip:$config_port/sys/test" -X GET)
  [[ -z "$testResult" ]] && {
    alarm " ERROR! Cannot connect to $config_ip:$config_port, method: sys/test"
    return 1
  }
}

function help() {
  echo "Usage:"
  echo "Optional:"
  echo "    -d                  [Optional]  disk directory to be monitor"
  echo "    -i                  [Require]  rpc server ip"
  echo "    -p                  [Require]  rpc server port"
  echo "    -T                  [Optional] disk capacity alarm threshold, default: 5%"
  echo "    -f                  [Optional] memory free alarm threshold, default: 5G"
  echo "    -h                  Help."
  echo "Example:"
  echo "    bash light_monitor.sh -i 127.0.0.1 -p 8250"
  echo "    bash light_monitor.sh -i 127.0.0.1 -p 8250 -d /data -T 10"
  exit 0
}

function params_must_set() {
  local name="$1"
  local params="$2"
  local flag="$3"
  [[ -z "${params}" ]] && {
    error "${name} must be set, you can use \'${flag}\' option to set it"
    exit 1
  }
}

while getopts "d:i:p:T:f:h" option; do
  case $option in
  d)
    disk_dir=$OPTARG
    dir_must_exists ${disk_dir}
    ;;
  i) rpc_ip=$OPTARG ;;
  p) rpc_port=$OPTARG ;;
  T) disk_space_threshold=$OPTARG ;;
  f) memory_free_threshold=$OPTARG ;;
  h) help ;;
  *) help ;;
  esac
done

params_must_set "rpc ip" "${rpc_ip}" "-i"
params_must_set "rpc port" "${rpc_port}" "-p"

# 磁盘容量检查
if [ -n "${disk_dir}" ]; then
  check_disk "${disk_dir}"
fi

check_memory

check_router_work_properly "${rpc_ip}" "${rpc_port}"
