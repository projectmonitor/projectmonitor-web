#!/usr/bin/env bash

MANIFEST_TO_MODIFY=$1
REVISION_SHA=$2

awk -v REVISION_SHA=${REVISION_SHA} '/^    storySha:/{ $0 = "    storySha: " REVISION_SHA }{print}' ${MANIFEST_TO_MODIFY} > manifest.yml

