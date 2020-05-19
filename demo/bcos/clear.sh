#!/bin/bash
if [ -e nodes/127.0.0.1/stop_all.sh ]; then
    bash nodes/127.0.0.1/stop_all.sh
fi
rm -rf console ipconf nodes accounts ledger-tool