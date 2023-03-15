#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd ${dirpath}
export LANG='zh_CN.utf8'

APPS_FOLDER=$(pwd)/apps
PLUGLIN_FOLDER=$(pwd)/plugin
CLASS_PATH=$(pwd)'/apps/*:lib/*:conf:plugin/*:./'
WINDS_CLASS_PATH=$(pwd)'/apps/*;lib/*;conf;plugin/*;./'

STATUS_STARTING="Starting"
STATUS_RUNNING="Running"
STATUS_STOPPED="Stopped"

SECURIY_FILE='./.wecross.security'

LOG_INFO() {
    echo -e "\033[32m$@\033[0m"
}

LOG_ERROR() {
    echo -e "\033[31m$@\033[0m"
}

show_version() {
  LOG_INFO "--------------------------------------------------------------------"
  LOG_INFO "Router version:" $(ls ${APPS_FOLDER} |awk '{gsub(/.jar$/,""); print}')
  LOG_INFO "Stub plugins: [" $(ls ${PLUGLIN_FOLDER} |awk '{gsub(/.jar$/,""); print}') "]"
  LOG_INFO "--------------------------------------------------------------------"
}

create_jvm_security() {
  if [[ ! -f ${SECURIY_FILE} ]];then
    echo "jdk.disabled.namedCurves = " > ${SECURIY_FILE}
    # LOG_INFO "create new file ${SECURIY_FILE}"
  fi
}

check_java_available() {
    # java version "9"
    # java version "1.8.0_281"
    # openjdk version "15.0.2" 2021-01-19
    java_version_string=$(java -version 2>&1 | head -n 1)
    LOG_INFO "java version: ${java_version_string}"

    # 9
    # 1.8.0_281
    # 15.0.2
    java_version=$(echo "${java_version_string}" | awk -F '"' '{print $2}')

    major_version=$(echo "${java_version}" | awk -F '.'  '{print $1}')
    minor_version=$(echo "${java_version}" | awk -F '.'  '{print $2}')

    temp_version=$(echo "${java_version}" | awk -F '.'  '{print $3}')

    patch_version=$(echo "${temp_version}" | awk -F '_'  '{print $1}')
    ext_version=$(echo "${temp_version}" | awk -F '_'  '{print $2}')

    LOG_INFO "java major: ${major_version} minor: ${minor_version} patch: ${patch_version} ext: ${ext_version}"

    # java version 1.8-
    [[ "${major_version}" -eq 1 ]] && [[ "${minor_version}" -lt 8 ]] && {
      LOG_ERROR "Unsupport Java version => ${java_version}"
      exit 1;
    }

      # java version 1.8.0_251
    [[ "${major_version}" -eq 1 ]] && [[ "${minor_version}" -eq 8 ]] && [[ "${ext_version}" -lt 251 ]] && {
      LOG_ERROR "Unsupport Java version => ${java_version}"
      exit 1;
    }

    # Support Java Version
    LOG_INFO "Java check OK!"
}

wecross_pid() {
    ps -ef | grep com.webank.wecross.Service | grep ${APPS_FOLDER} | grep -v grep | awk '{print $2}'
}

run_wecross() {
    if [ "$(uname)" == "Darwin" ]; then
        # Mac
        nohup java -Dfile.encoding=UTF-8 -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative="false" -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448,ffdhe2048,ffdhe3072,ffdhe4096,ffdhe6144,ffdhe8192" -cp ${CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    elif [ "$(uname -s | grep MINGW | wc -l)" != "0" ]; then
        # Windows
        nohup java -Dfile.encoding=UTF-8 -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative="false" -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448,ffdhe2048,ffdhe3072,ffdhe4096,ffdhe6144,ffdhe8192" -cp ${WINDS_CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    else
        # GNU/Linux
        nohup java -Dfile.encoding=UTF-8 -Djdk.tls.client.protocols=TLSv1.2 -Djava.security.properties=${SECURIY_FILE} -Djdk.sunec.disableNative="false" -Djdk.tls.namedGroups="SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448,ffdhe2048,ffdhe3072,ffdhe4096,ffdhe6144,ffdhe8192" -cp ${CLASS_PATH} com.webank.wecross.Service >start.out 2>&1 &
    fi
}

wecross_status() {
    if [ ! -z $(wecross_pid) ]; then
        if [ ! -z "$(grep "WeCross router start success" start.out)" ]; then
            echo ${STATUS_RUNNING}
        else
            echo ${STATUS_STARTING}
        fi
    else
        echo ${STATUS_STOPPED}
    fi
}

tail_log() {
    # LOG_INFO "Debug log"
    # cat logs/debug.log
    LOG_INFO "Error log"
    tail -n 1000 logs/error.log
    LOG_INFO "Start log"
    tail -n 50 start.out
}

before_start() {
    local status=$(wecross_status)

    case ${status} in
    ${STATUS_STARTING})
        LOG_ERROR "WeCross is starting, pid is $(wecross_pid)"
        exit 0
        ;;
    ${STATUS_RUNNING})
        LOG_ERROR "WeCross is running, pid is $(wecross_pid)"
        exit 0
        ;;
    ${STATUS_STOPPED})
        # do nothing
        ;;
    *)
        exit 1
        ;;
    esac
}

start() {
    rm -f start.out
    show_version
    check_java_available
    create_jvm_security
    run_wecross
    echo -e "\033[32mWeCross booting up ..\033[0m\c"
    try_times=45
    i=0
    while [ $i -lt ${try_times} ]; do
        sleep 1
        local status=$(wecross_status)

        case ${status} in
        ${STATUS_STARTING})
            echo -e "\033[32m.\033[0m\c"
            ;;
        ${STATUS_RUNNING})
            break
            ;;
        ${STATUS_STOPPED})
            break
            ;;
        *)
            exit 1
            ;;
        esac

        ((i = i + 1))
    done
    echo ""
}

after_start() {
    local status=$(wecross_status)

    case ${status} in
    ${STATUS_STARTING})
        kill -9 $(wecross_pid)
        LOG_ERROR "Exceed waiting time. Killed. Please try to start WeCross again"
        tail_log
        exit 1
        ;;
    ${STATUS_RUNNING})
        LOG_INFO "WeCross start successfully!"
        ;;
    ${STATUS_STOPPED})
        LOG_ERROR "WeCross start failed"
        LOG_ERROR "See logs/error.log for details"
        tail_log
        exit 1
        ;;
    *)
        exit 1
        ;;
    esac
}

main() {
    before_start
    start
    after_start
}

main
