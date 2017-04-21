#!/usr/bin/env bash


if [[ "$ShaToBuild" == "0" ]] ; then
  echo "Cannot build from SHA: $ShaToBuild"
  exit 1
fi

echo "Building SHA $ShaToBuild"
git co $ShaToBuild

shouldDeploy=$(git log HEAD~..HEAD | sed '1,/^$/d' | egrep "\[finishes\s+#[0-9]+\]|\[fixes\s+#[0-9]+\]|\[completes\s+#[0-9]+\]" | awk '{print}' )
if [ ! -z "$shouldDeploy" ]
then

    mvn clean package -DskipTests

    ./ci/deliver_tracker_story.sh

    ./ci/add_story_to_manifest.sh manifest-acceptance.yml ${ShaToBuild}

    cf login -a api.run.pivotal.io -u ${CF_USER} -p ${CF_PASSWORD} -s pronto -o dirk
    cf push -f manifest.yml -t 180
    cf logout


else
    mkdir -p target
    touch target/fake-SNAPSHOT.jar
fi

git co master


