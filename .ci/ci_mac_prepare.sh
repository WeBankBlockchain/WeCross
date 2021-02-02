#!/bin/bash

DB_PASSWORD=${CI_DB_PASSWORD}
DOCKER_SIGNAL=${CI_DOCKER_SIGNAL}

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

check_command() {
    local cmd=${1}
    if [ -z "$(command -v "${cmd}")" ]; then
        LOG_ERROR "${cmd} is not installed."
        exit 1
    fi
}

check_docker_service() {
    docker ps
    if ! docker ps >/dev/null; then
        LOG_INFO "Please install docker."
        exit 1
    fi
}

# install docker
if [ "${DOCKER_SIGNAL}" ]; then
    brew install docker-machine docker-compose docker

    docker-machine create --driver virtualbox default
    docker-machine env default
    eval "$(docker-machine env default)"

    LOG_INFO "Check environments"
    check_command docker
    check_command docker-compose

    check_docker_service
fi

brew install expect tree md5sha1sum expect mysql

if [ ! "${DB_PASSWORD}" ]; then
    DB_PASSWORD='123456'
fi

mysql.server start

cd .

brew services start mysql

expect <<EOF
spawn mysql_secure_installation 2>/dev/null
expect "Press y|Y for Yes, any other key for No:"
send "n\r"
expect "New password:"
send "${DB_PASSWORD}\r"
expect "Re-enter new password:"
send "${DB_PASSWORD}\r"
expect "Remove anonymous users? (Press y|Y for Yes, any other key for No) :"
send "n\r"
expect "Disallow root login remotely? (Press y|Y for Yes, any other key for No) :"
send "n\r"
expect "Remove test database and access to it? (Press y|Y for Yes, any other key for No) :"
send "n\r"
expect "Reload privilege tables now? (Press y|Y for Yes, any other key for No) :"
send "n\r"
expect eof
EOF

LOG_INFO "Check environments"
check_command mysql
