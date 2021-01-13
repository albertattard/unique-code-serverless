#!/bin/bash

GOOS=linux go build -o bin/main main.go
cd bin
zip unique-code-serverless.zip main
