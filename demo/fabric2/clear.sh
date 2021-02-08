#!/bin/bash
samples_version=2.3.0
samples_dir=fabric-samples-${samples_version}/test-network
if [ -d ${samples_dir} ]; then
    cd ${samples_dir}
    bash samples.sh down <<EOF
Y
EOF
    cd -
fi

rm -rf fabric-samples-${samples_version} config bin certs
