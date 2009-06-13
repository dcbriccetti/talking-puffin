#!/bin/bash

JAR="talking-puffin-${1}-jar-with-dependencies.jar"

# jarsigner doesn’t like the duplicate files (manifests and such) in the jar, so we remove them
# with jar extract and create
rm -Rf /tmp/a
mkdir /tmp/a
mv target/$JAR /tmp/a
pushd /tmp/a
jar xf $JAR 
rm $JAR
jar cf $JAR *

jarsigner $JAR mykey2
popd
mv /tmp/a/$JAR ../website/jws
