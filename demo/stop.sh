#!/bin/bash
run_if_exists() {
    local script=${1}
    if [ -e ${script} ]; then
        bash ${script}
    fi
}

run_if_exists bcos/stop.sh
run_if_exists fabric/stop.sh
run_if_exists fabric2/stop.sh
run_if_exists routers-payment/stop_all.sh