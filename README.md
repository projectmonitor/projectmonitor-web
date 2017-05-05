# About Project Monitor
## WARNING: This is most certainly Alpha software! This guide was last touched on : May 5th, 2017

### Continuous Delivery?
This projects goal is support continuous delivery by building a workflow about Pivotal Tracker.  

Continuous delivery implies a lot things about the maturity level of your organization (automated test suites, automated deploys, automated rollbacks, monitoring etc.)  We hope to help by reducing the number of road blocks a project has to get to CD with (hopefully) little effort.

# End User Readme
### Assumptions
* you are using pivotal tracker for your backlog
* you are using git for version management
* you are using jenkins for CI

**We want to support more tools, but that's how it is in the alpha.**

### The dealio
Currently, Project Monitor only supports a feature branching workflow.  
As a developer, you start a story, create a feature branch.  When done with the story, push a commit with the fixes, completes, finishes tags tracker supports.  You should not mark the  story delivered as that will be handled by Project Monitor when it deploys to story acceptance.

CI is running for all branches (except master, which should be locked down for commits).  If a commit completes a story, a build is enqueued to be deployed to the Story Acceptance environment.  Commits that do not complete a story will run CI but will not be enqueued to go to story acceptance.

Enqueued is an important distinction here.  Stories (and therefore branches) will be accepted in isolation from each other.  So builds must be queued, only being deployed once the previous story has been accepted. This ensures that there is not pollution from other stories that have been recently completed.  There will be a production deploy of each accepted story in the order in which they were completed/accepted.

Before a build is sent to SA, it is merged with master (a no edit merge).  This ensures that the branch is up to date with previously deployed stories.  Once the story has been accepted by the PM in tracker the branch is merged into master and deployed to production.  The next available buld is then deployed to story acceptance.  Conflicts currently fail the job, to be handled manually for the moment.

If a story is rejected, no other builds will be deployed until that story has been accepted.  Pushing a fixes commit to the branch of the rejected story will cause a new deploy to go to Story Acceptance for that story.

### Your app setup
1. We expect you have endpoint at /info. This returns json and contains the sha deployed and the story deployed to that environment
1. Project monitor will set environment variables in your manifest file before deploy with the correct values.  if you are using spring boot actuator, you can drop the InfoConfiguration class into your project and you'r done.
Example response:
```json
{
    "pivotalTrackerStoryID": "144644741",
    "storySHA": "bbd82fc105e68d85956d6eacf3cfb58ddc8ab04f"
}
```

### Jenkins Setup
Jenkins currently works off of 3 jobs:
1. A CI Job (triggered by pushes to your repo, any branch exacpt master)
 * runs tests
 * enqueues build for deploy to story acceptance if necessary
2. A Job to deploy to Story Acceptance (triggered by project monitor programmatically)
 * no edit merges with master
 * re runs test on merge
 * deploys to story acceptance
 * marks story as delivered
3. A job to deploy to Production (triggered by project monitor programmatically)
 * checks out sha deployed to acceptance
 * rebuilds artifacts
 * deploys to production

We have scripts for each jenkins job in the ci directory. Currently you will need to copy these scripts to your project and make edits as necessary for your projects (test commands/deploy command etc.).  

**Notes:**
 * If a merge or deploy to Story acceptance fails the relevant story is set to rejected by Project monitor.
 * Project monitor displays information about most of what I just covered on the home page
 * Currently, an install of project monitor is just configured for one app (feature in the backlog)
 * Pulling a build out of the queue is a manual process. This can be accomplished by manually deploying a different build to Story Acceptance
 * Most state that project monitor uses is contained in jenkins and tracker, that means info you want to see should be one of those tow places
 

### Environment variables
There are several environment variables that project monitor will need to operate.  You can find examples in the application.properties file


##### NEED TO GET EXAMPLES OF THE JOBS, guides to setting them up!

#Developer README
Assuming you have cloned the repo somewheres relevant...

install maven:
```bash
brew install maven
```

install dependencies:
```bash
mvn install
```

run test suite:
```bash
mvn test
```

### Install Jenkins locally
For development you probably want a local jenkins install:
```bash
brew install jenkins
brew services start jenkins
```

### Install Redis Locally
We make use of redis to queue builds for deliver to Story Acceptance:
```bash
brew install redis
brew services start redis
```

**Ignore this for now: Install bats for bash unit tests:**
```
brew install bats

# bats mocks has no brew, git sumodule

brew tap kaos/shell
brew install bats-assert
brew install bats-file
```