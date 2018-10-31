#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR

git pull && \
    git submodule update && \
    COMMIT_ID=$(git rev-parse --short HEAD) docker-compose -f docker-compose.yml -f docker-compose.prod.yml build && \
    COMMIT_ID=$(git rev-parse --short HEAD) docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
