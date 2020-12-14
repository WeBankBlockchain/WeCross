#!/bin/bash
if [ -e nodes/127.0.0.1/stop_all.sh ]; then
    bash nodes/127.0.0.1/stop_all.sh
fi

if [ -e nodes_gm/127.0.0.1/stop_all.sh ]; then
    bash nodes_gm/127.0.0.1/stop_all.sh
fi

rm -rf accounts console ipconf nodes nodes_gm accounts ledger-tool
