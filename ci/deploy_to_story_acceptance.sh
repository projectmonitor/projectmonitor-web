#!/usr/bin/env bash
set -euf -o pipefail

# because of how Jenkins checks out from Git, we are not on a branch and the remote branches are not known locally
git checkout master
git branch -va


if [[ "$ShaToBuild" == "0" ]] ; then
  echo "Cannot build from SHA: $ShaToBuild"
  exit 1
fi

ShaToBuild=$(git rev-parse $ShaToBuild)
echo "Building SHA $ShaToBuild"

# find branch we are on and pull remote branch in
branch_name=$(git branch -a --contains $ShaToBuild | awk '{n=split($1,parts,"/"); print parts[n]}FNR==2{print "Branch could not be determined";exit 7}' )
git checkout ${branch_name}

top_sha_of_branch=$(git log -1 -r ${branch_name} | awk 'FNR==1{print $2}' )
if [[ "$top_sha_of_branch" != "$ShaToBuild" ]] ; then
   echo "This SHA ($ShaToBuild) is not at the top of the feature branch ${branch_name}"
   exit 2
fi

# dangerous, the sed command below depends on pipefail being off, which is not best practice
# THIS AWK command should work with set -o pipefial, but it does not catch the completes right (note that the awk commands also reverses
# the logic, exit 1 on found.
# awk '/(\[finishes\s+#[0-9]+\])|(\[fixes\s+#[0-9]+\])|(\[completes\s+#[0-9]+\])/{result=1}END{print "RES" result; exit result}';
set +o pipefail

shouldDeploy=$(git log HEAD~..HEAD | egrep "\[finishes\s+#[0-9]+\]|\[fixes\s+#[0-9]+\]|\[completes\s+#[0-9]+\]" | awk '{print}' )
if [ ! -z "$shouldDeploy" ]
then
    git merge master --no-edit || ( echo "### The merge with master failed" && git reset --hard origin/master && exit 4 )

    mvn clean package || ( echo "### The tests or build failed" && exit 5  )

    git push origin $branch_name

    ShaRebased=$(git rev-parse HEAD)
    ./ci/deliver_tracker_story.sh
    ./ci/add_story_to_manifest.sh manifest-acceptance.yml ${ShaRebased}

    cf login -a api.run.pivotal.io -u ${CF_USER} -p ${CF_PASSWORD} -s pronto -o dirk
    cf push -f manifest.yml -t 180
    cf logout
else
    mkdir -p target
    touch target/fake-SNAPSHOT.jar
fi

git checkout master
git reset --hard origin/master
