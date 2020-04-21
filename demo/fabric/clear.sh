#!/bin/bash
cd fabric-samples-1.4.4/first-network
bash byfn.sh down <<EOF
Y
EOF
cd -

rm -rf fabric-samples-1.4.4 config bin certs