#!/usr/bin/env bash
set -euf -o pipefail

mvn clean test

# because of how Jenkins checks out from Git, we are not on a branch and the remote branches are not known locally
git checkout master
git branch -va

# find branch we are on and pull remote branch in
branch_name=$(git branch -a --contains $GIT_COMMIT | awk '{n=split($1,parts,"/"); print parts[n]}FNR==2{print "Branch could not be determined";exit 7}' )
git checkout ${branch_name}

top_sha_of_branch=$(git log -1 -r ${branch_name} | awk 'FNR==1{print $2}' )
if [[ "$top_sha_of_branch" != "$GIT_COMMIT" ]] ; then
   echo "This SHA ($GIT_COMMIT) is not at the top of the feature branch ${branch_name}"
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
    tracker_tag="$(git log -1 | egrep "\[finishes\s+#[0-9]+\]|\[fixes\s+#[0-9]+\]|\[completes\s+#[0-9]+\]" | awk '{print $2, $3}')"
    STORY_ID=$(echo $tracker_tag | egrep -o "[0-9]+")
    curl -X PUT "http://localhost:8081/storyAcceptanceDeploy/${GIT_COMMIT}-${STORY_ID}"
else
    mkdir -p target
    touch target/fake-SNAPSHOT.jar
fi