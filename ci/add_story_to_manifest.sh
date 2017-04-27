#!/usr/bin/env bash

MANIFEST_TO_MODIFY=$1
REVISION_SHA=$2
STORY_ID=$3

line_number=$(cat -n ${MANIFEST_TO_MODIFY} | grep storySha | awk '{print $1}')
sed "${line_number}s/.*/    storySha: ${REVISION_SHA}/" ${MANIFEST_TO_MODIFY} > manifesttemp.yml

line_number=$(cat -n manifesttemp.yml | grep pivotalTrackerStoryID | awk '{print $1}')
sed "${line_number}s/.*/    pivotalTrackerStoryID: ${STORY_ID}/" manifesttemp.yml > manifest.yml

rm manifesttemp.yml

cat manifest.yml
