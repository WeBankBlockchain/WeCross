#!/bin/bash

run_if_exists() {
    local script=${1}
    if [ -e ${script} ]; then
        bash ${script}
    fi
}

run_if_exists bcos/start.sh
run_if_exists fabric/start.sh
run_if_exists fabric2/start.sh
run_if_exists routers-payment/start_all.sh


