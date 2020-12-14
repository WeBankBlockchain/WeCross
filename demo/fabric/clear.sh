#!/bin/bash
byfn_dir=fabric-samples-1.4.4/first-network
if [ -d ${byfn_dir} ]; then
    cd ${byfn_dir}
    bash byfn.sh down <<EOF
Y
EOF
    cd -
fi

rm -rf fabric-samples-1.4.4 config bin certs
