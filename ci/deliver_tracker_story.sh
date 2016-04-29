#!/bin/bash

set -ex

cd projectmonitor-web

function update_tracker_story {
  curl -X PUT -H "X-TrackerToken: $TRACKER_TOKEN" -H "Content-Type: application/json" -d '{"current_state": "delivered"}' "https://www.pivotaltracker.com/services/v5/projects/$TRACKER_PROJECT_ID/stories/$STORY_ID"
}

function determine_if_commit_finishes_story {
  tracker_tag="$(git log HEAD~..HEAD | sed '1,/^$/d' | egrep "\[finishes\s+#[0-9]+\]|\[fixes\s+#[0-9]+\]|\[completes\s+#[0-9]+\]" | awk '{print $2, $3}')"
  found_finishes_tag=$?
}

function grab_story_id {
  STORY_ID=$(echo $tracker_tag | awk '{print $2}' | egrep -o "[0-9]+")
}

if [ $found_finishes_tag -eq 0 ]
then
  determine_if_commit_finishes_story
  grab_story_id
  update_tracker_story
fi

