#Developer Setup
Please run the setup script before starting work
```bash
$: ./setup.sh
```

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

To commit on this project you must use our git command:
```bash
git hard
```
This will require a story number and then allow you to enter a message.

ci is on travis:
```
https://travis-ci.org/projectmonitor/projectmonitor-web
```

app is not running anywheres currently

**Ignore this for now: Install bats for bash unit tests:**
```
brew install bats

# bats mocks has no brew, git sumodule

brew tap kaos/shell
brew install bats-assert
brew install bats-file
```