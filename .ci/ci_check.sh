#!/bin/bash

set -e

./gradlew verifyGoogleJavaFormat
./gradlew build -x test
./gradlew test -i
./gradlew jacocoTestReport
