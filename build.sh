#!/bin/bash
set -e

echo "-------------"
echo "Build project"
echo "-------------"
npm run build
mvn verify
