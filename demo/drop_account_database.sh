#!/bin/bash
set -e

LANG=en_US.UTF-8

DB_IP=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123456
DB_NAME=wecross_account_manager
NEED_ASK=true

LOG_INFO() {
    # shellcheck disable=SC2145
    echo -e "\033[32m[INFO] $@\033[0m"
}

LOG_ERROR() {
    # shellcheck disable=SC2145
    echo -e "\033[31m[ERROR] $@\033[0m"
}

# shellcheck disable=SC2120
help() {
    echo "$1"
    cat <<EOF
Drop wecross-account-manager database named ${DB_NAME}.
Usage:
    -d                              [Optional] Use default db configuration: -H ${DB_IP} -P ${DB_PORT} -u ${DB_USERNAME} -p ${DB_PASSWORD}
    -H                              [Optional] DB ip
    -P                              [Optional] DB port
    -u                              [Optional] DB username
    -p                              [Optional] DB password
    -h  call for help
e.g
    bash $0 -H ${DB_IP} -P ${DB_PORT} -u ${DB_USERNAME} -p ${DB_PASSWORD}
    bash $0
EOF
    exit 0
}

parse_command() {
    while getopts "H:P:u:p:dh" option; do
        # shellcheck disable=SC2220
        case ${option} in
        d)
            NEED_ASK=false
            ;;
        H)
            DB_IP=$OPTARG
            NEED_ASK=false
            ;;
        P)
            DB_PORT=$OPTARG
            NEED_ASK=false
            ;;
        u)
            DB_USERNAME=$OPTARG
            NEED_ASK=false
            ;;
        p)
            DB_PASSWORD=$OPTARG
            NEED_ASK=false
            ;;
        h) help ;;
        *) help ;;
        esac
    done
}

db_config_ask() {
    LOG_INFO "Database connection:"
    read -r -p "[1/4]> ip: " DB_IP
    read -r -p "[2/4]> port: " DB_PORT
    read -r -p "[3/4]> username: " DB_USERNAME
    read -r -p "[4/4]> password: " -s DB_PASSWORD
    echo "" # \n
    LOG_INFO "Database connetion with: ${DB_IP}:${DB_PORT} ${DB_USERNAME} "
    check_db_service
}

query_db() {
    mysql -u "${DB_USERNAME}" --password="${DB_PASSWORD}" -h "${DB_IP}" -P "${DB_PORT}" "$@" 2>/dev/null
}

drop_db() {
    mysql -u "${DB_USERNAME}" --password="${DB_PASSWORD}" -h "${DB_IP}" -P "${DB_PORT}" <<EOF
    drop database ${DB_NAME};
    exit
EOF
}

config_database() {
    if ${NEED_ASK}; then
        db_config_ask
    else
        check_db_service
    fi
}

check_db_service() {
    LOG_INFO "Checking database configuration"
    set +e
    if ! query_db -e "status;" ; then
        LOG_ERROR "Failed to connect database, please try again."
        exit 1
    fi
    set -e
}

parse_command "$@"
config_database
drop_db
LOG_INFO "Drop database successfully."
