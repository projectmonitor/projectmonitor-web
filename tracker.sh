#!/bin/sh

read -p "Tracker Story ID (e.g. #118149757): " TRACKER_ID

echo "pivotalTrackerStoryID=$TRACKER_ID" > "./src/main/resources/tracker.properties"
git add ./src/main/resources/tracker.properties

git commit "$@"
