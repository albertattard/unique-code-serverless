FROM ekidd/rust-musl-builder:1.49.0 AS builder
RUN sudo apt-get update && sudo apt-get install zip -y

CMD cargo build --release \
    && zip -j ./target/x86_64-unknown-linux-musl/release/bootstrap.zip ./target/x86_64-unknown-linux-musl/release/bootstrap
