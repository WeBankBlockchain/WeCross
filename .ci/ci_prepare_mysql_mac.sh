#!/bin/bash

DB_PASSWORD=${CI_DB_PASSWORD}

brew install mysql

if [ ! ${DB_PASSWORD} ]; then
    DB_PASSWORD='123456'
fi

brew services start mysql

mysql.server start

cd .

mysql_secure_installation <<EOF
N
${DB_PASSWORD}
${DB_PASSWORD}
N
N
N
N
EOF
