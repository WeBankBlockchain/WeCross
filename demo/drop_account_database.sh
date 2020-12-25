#!/bin/bash
set -e

LANG=en_US.UTF-8

DB_IP=127.0.0.1
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123456
DB_NAME=wecross_account_manager

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
    bash $0 -d
EOF
    exit 0
}

parse_command() {
    while getopts "H:P:u:p:h" option; do
        # shellcheck disable=SC2220
        case ${option} in
        H)
            DB_IP=$OPTARG
            ;;
        P)
            DB_PORT=$OPTARG
            ;;
        u)
            DB_USERNAME=$OPTARG
            ;;
        p)
            DB_PASSWORD=$OPTARG
            ;;
        h) help ;;
        *) help ;;
        esac
    done
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
check_db_service
drop_db
LOG_INFO "Drop database successfully."
