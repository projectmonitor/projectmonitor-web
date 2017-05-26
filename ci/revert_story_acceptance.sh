#!/usr/bin/env bash

set -euf -o pipefail

# REPLACE ME WITH YOUR ORG AND SPACE
export CF_SPACE=example_space
export CF_ORG=example_org

# because of how Jenkins checks out from Git, we are not on a branch and the remote branches are not known locally
git checkout master
git branch -va

if [[ "$SHA_TO_BUILD" == "0" ]] ; then
  echo "Cannot build from SHA: $SHA_TO_BUILD"
  exit 1
fi

if [[ "$STORY_ID" == "0" ]] ; then
  echo "Please set STORY_ID, we had: $STORY_ID"
  exit 1
fi

SHA_TO_BUILD=$(git rev-parse $SHA_TO_BUILD)
echo "Building SHA $SHA_TO_BUILD"

git checkout $SHA_TO_BUILD

mvn clean package -DskipTests

./ci/add_story_to_manifest.sh manifest-acceptance.yml ${SHA_TO_BUILD} ${STORY_ID}

cf login -a api.run.pivotal.io -u ${CF_USER} -p ${CF_PASSWORD} -s $CF_SPACE -o $CF_ORG
cf push -f manifest.yml -t 180
cf logout

git checkout master