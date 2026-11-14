#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
rm -rf target/classes target/test-classes
mkdir -p target/classes target/test-classes
find src/main/java -name '*.java' > target/sources.txt
javac --release 21 -encoding UTF-8 -d target/classes @target/sources.txt
jar --create --file target/career-agent.jar --main-class career.CliApp -C target/classes .
javac --release 21 -encoding UTF-8 -cp target/classes -d target/test-classes src/test/java/career/SelfTest.java src/test/java/career/ApiAdaptersTest.java src/test/java/career/RepositoryTest.java src/test/java/career/InputFeaturesTest.java
java -ea -cp target/classes:target/test-classes career.SelfTest
