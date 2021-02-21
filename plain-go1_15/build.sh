#!/bin/bash

GOOS=linux go build -o bin/main github.com/albertattard/unique-code-serverless/src
cd bin
zip unique-code-serverless.zip main
