#!/bin/bash
samples_version=2.3.0
samples_dir=fabric-samples-${samples_version}/test-network
if [ -d ${samples_dir} ]; then
    cd ${samples_dir}
    bash network.sh down <<EOF
Y
EOF
    sleep 10 # waiting container to exit
    cd -
fi

rm -rf fabric-samples-${samples_version} config bin certs
