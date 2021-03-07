#!/bin/bash
set -e

echo "-------------"
echo "Build project"
echo "-------------"
npm run build
mvn verify -DskipTests

echo ""
echo "----------------------------"
echo "Build Linux class path image"
echo "----------------------------"
echo ""
jlink \
	--compress=2 --no-header-files --no-man-pages --strip-debug \
	--output target/linux-image \
	--add-modules java.desktop,java.naming
mkdir target/linux-image/app
cp target/calendar.jar target/linux-image/app
cp src/image/application.properties target/linux-image/app
cp src/image/calendar target/linux-image/bin
tar -czf target/calendar.tar.gz target/linux-image
