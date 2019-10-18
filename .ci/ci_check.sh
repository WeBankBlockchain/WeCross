#!/bin/bash

set -e
./gradlew verifyGoogleJavaFormat
./gradlew build
./gradlew test
./gradlew jacocoTestReport