#!/bin/bash

mvn clean install -DskipTests

for m in org.eclipse.jgit org.eclipse.jgit.ssh.apache; do
  mvn install:install-file \
    -Dfile=$m/target/$m-LOCAL.jar \
    -DgroupId=com.github.appsmithorg.jgit \
    -DartifactId=$m \
    -Dversion=main-SNAPSHOT \
    -Dpackaging=jar
done
