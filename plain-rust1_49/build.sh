#!/bin/bash

docker run --rm \
     -v $(pwd):/home/rust/src \
     -v ${HOME}/.cargo/registry:/home/rust/.cargo/registry \
     -v ${HOME}/.cargo/git:/home/rust/.cargo/git \
     -it plain-rust1_49-builder:local
