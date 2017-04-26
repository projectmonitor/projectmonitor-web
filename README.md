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
git deliver
```
This will require a story number and then allow you to enter a message.

### Install Jenkins locally
```bash
brew install jenkins
brew services start jenkins
```

### Install Redis Locally
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