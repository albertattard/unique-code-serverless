#!/bin/bash

set -euo pipefail

./gradlew clean check bootBuildImage
