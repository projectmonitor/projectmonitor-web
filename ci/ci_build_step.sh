#!/usr/bin/env bash

export PATH=${PATH}:/usr/local/bin
echo $PATH
mvn clean test
curl "http://localhost:8080/job/TestProject to SA/build?ShaToBuild=$GIT_COMMIT"

