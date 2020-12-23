#!/bin/bash

DB_PASSWORD=${CI_DB_PASSWORD}

brew install mysql

if [ ! ${DB_PASSWORD} ]; then
    DB_PASSWORD='123456'
fi

mysql.server start

cd .

brew services start mysql

mysql_secure_installation <<EOF
n
123456
123456
n
n
n
n
EOF
