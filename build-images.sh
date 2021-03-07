#!/bin/bash
set -e

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
cp src/image/calendar target/linux-image
tar -czf target/calendar-linux.tar.gz -C target/linux-image .
echo "Done"

echo ""
echo "------------------------------"
echo "Build Windows class path image"
echo "------------------------------"
echo ""
curl --silent https://download.java.net/java/GA/jdk16/7863447f0ab643c585b9bdebf67c69db/36/GPL/openjdk-16_windows-x64_bin.zip > target/windows-jdk.zip
unzip -q target/windows-jdk.zip -d target/windows-jdk
jlink \
	--module-path target/windows-jdk/jdk-16/jmods \
	--compress=2 --no-header-files --no-man-pages --strip-debug \
	--output target/windows-image \
	--add-modules java.desktop,java.naming
mkdir target/windows-image/app
cp target/calendar.jar target/windows-image/app
cp src/image/application.properties target/windows-image/app
cp src/image/calendar.bat target/windows-image
cd target/windows-image && zip -qr ../calendar-windows.zip * && cd -
echo "Done"
