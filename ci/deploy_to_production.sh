#!/usr/bin/env bash
set -euf -o pipefail

# because of how Jenkins checks out from Git, we are not on a branch and the remote branches are not known locally
git checkout master
git branch -va

if [[ "$SHA_TO_DEPLOY" == "0" ]] ; then
  echo "Cannot deploy SHA: $SHA_TO_DEPLOY"
  exit 1
fi

SHA_TO_DEPLOY=$(git rev-parse $SHA_TO_DEPLOY)
echo "Building SHA $SHA_TO_DEPLOY"

branch_name=$(git branch -a --contains $SHA_TO_DEPLOY | awk '{n=split($1,parts,"/"); print parts[n]}FNR==2{print "Branch could not be determined";exit 7}' )
git checkout ${branch_name}

top_sha_of_branch=$(git log -1 -r ${branch_name} | awk 'FNR==1{print $2}' )
if [[ "$top_sha_of_branch" != "$SHA_TO_DEPLOY" ]] ; then
   echo "This SHA ($SHA_TO_DEPLOY) is not at the top of the feature branch ${branch_name}"
   exit 2
fi

# merge branch into master and push it out
git checkout master
git merge $branch_name --ff-only || ( echo "Fast forward branch merge failed" && git reset --hard origin/master && exit 4 )

mvn clean package -DskiptTests
git push origin master

# deploy to production environment
./ci/add_story_to_manifest.sh manifest-production.yml ${SHA_TO_DEPLOY}

cf login -a api.run.pivotal.io -u ${CF_USER} -p ${CF_PASSWORD} -s pronto -o dirk
cf push -f manifest.yml -t 180
cf logout

git checkout master