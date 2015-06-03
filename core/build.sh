#!/bin/bash
set -o errexit

mvn -Dmaven.test.skip=true clean compile package
mvn -Dmaven.test.skip=true assembly:single
