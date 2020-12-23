#!/bin/bash

DB_PASSWORD=${CI_DB_PASSWORD}

brew install mysql

if [ ! ${DB_PASSWORD} ]; then
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
