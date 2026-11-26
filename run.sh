#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD="$PROJECT_DIR/build"
LIB="$PROJECT_DIR/lib"

CLASSPATH="$BUILD:$LIB/*"

java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" Main