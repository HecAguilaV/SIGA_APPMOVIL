#!/bin/bash
set -euo pipefail

cd /c/Users/hdagu/Documents/DevAppMobile || exit 1
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME=$JAVA_HOME"
which java
java -version
./gradlew uninstallDebug && ./gradlew installDebug
