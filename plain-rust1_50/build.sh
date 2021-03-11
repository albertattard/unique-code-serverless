#!/bin/bash

docker build . -f builder/Dockerfile -t plain-rust1_50-builder:local

docker run --rm \
     -v "$(pwd):/home/rust/src" \
     -v "${HOME}/.cargo/registry:/home/rust/.cargo/registry" \
     -v "${HOME}/.cargo/git:/home/rust/.cargo/git" \
     -it plain-rust1_50-builder:local
