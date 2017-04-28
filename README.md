# About the Last Mile
## This is most certainly Alpha software, so lulz ahead I'm sure.

### Continuous Delivery?
This projects goal is support continuous delivery by building a workflow about Pivotal Tracker.  

Continuous delivery implies a lot things about the maturity level of your organization (automated test suites, automated deploys, automated rollbacks, monitoring etc.)  We hope to help by reducing the number of road blocks a project has to get to CD with (hopefully) little effort.

# End User Setup
### Assumptions
* you are using pivotal tracker for your backlog
* you are using git for version management
* you are using jenkins for CI

**We want to support more tools, but that's how it is in the alpha.**

### The dealio
Currently, Last Mile only supports a feature branching workflow.  
As a developer, you start a story, create a feature branch.  When done with the story, push a commit with the fixes, completes, finishes tags tracker supports.  You should not mark the  story delivered as that will be handled by Last Mile when it deploys to story acceptance.

CI is running for all branches (except master, which should be locked down for commits).  If a commit completes a story, a build is enqueued to be deployed to the Story Acceptance environment.  Commits that do not complete a story will run CI but will not be enqueued to go to story acceptance.

Enqueued is an important distinction here.  Stories will be accepted in isolation from each other.  So builds must be queued, only being deployed once the previous story has been accepted. This ensures that there is not pollution from other stories that have been recently completed.  There will be a production deploy of each accepted story in the order in which they were completed/accepted.

Before a build is sent to SA, it is merged with master (a no edit merge).  This ensures that the branch is up to date with previously deployed stories.  Once the story has been accepted by the PM in tracker the branch is merged into master and deployed to production.  The next available buld is then deployed to story acceptance.  Conflicts currently fail the job, to be handled manually for the moment.

# This documentation is currently incomplete. SO use is not currently advised!!!!

Please run the setup script before starting work
```bash
$: ./setup.sh
```

#Developer Setup
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