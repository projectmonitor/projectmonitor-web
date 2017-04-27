#!/bin/sh

#read -p "Tracker Story ID (e.g. #118149757): " TRACKER_ID

#awk -v TRACKER_ID=${TRACKER_ID} '/^pivotalTrackerStoryID=/{ $0 = "pivotalTrackerStoryID=" TRACKER_ID }{print}' "./src/main/resources/tracker.properties" > "./src/main/resources/tracker.properties.new"
#mv "./src/main/resources/tracker.properties.new"  "./src/main/resources/tracker.properties"
#
#git add ./src/main/resources/tracker.properties

git commit "$@"
