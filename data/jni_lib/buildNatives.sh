#!/bin/bash

cargo ndk -t armeabi-v7a -t arm64-v8a -t x86_64 -o ../src/main/jniLibs build --profile release-space-optimized