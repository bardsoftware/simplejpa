#!/bin/sh
mvn -Dmaven.test.skip=true clean compile assembly:single
