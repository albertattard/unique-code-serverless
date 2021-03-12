#!/bin/bash

set -euo pipefail

# Create the native image
docker build . -f builder/Dockerfile -t micronaut-graalvm-java11-builder

# Copy the native image created above to the build directory
[ -d build ] || mkdir build
docker run --rm --entrypoint cat micronaut-graalvm-java11-builder /home/application/function.zip >build/function.zip
