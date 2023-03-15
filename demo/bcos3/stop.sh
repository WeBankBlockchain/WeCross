#!/bin/bash

SHELL_FOLDER="$(cd "$(dirname "$0")" && pwd)"

if [ -e ${SHELL_FOLDER}/nodes/127.0.0.1/stop_all.sh ]; then
    bash ${SHELL_FOLDER}/nodes/127.0.0.1/stop_all.sh
fi

if [ -e ${SHELL_FOLDER}/nodes_gm/127.0.0.1/stop_all.sh ]; then
    bash ${SHELL_FOLDER}/nodes_gm/127.0.0.1/stop_all.sh
fi
