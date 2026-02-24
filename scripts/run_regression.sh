#!/bin/bash

# run_regression.sh
# Executes the full validation suite for tap-tap-boom

echo "========================================="
echo "  🚀 Starting Regression Suite           "
echo "========================================="

# 2. JVM Unit Tests (Domain, Data, UI)
echo "-----------------------------------------"
echo "1. Running Shared JVM Unit Tests"
./gradlew :shared:domain:jvmTest :shared:data:jvmTest :shared:ui:jvmTest
if [ $? -ne 0 ]; then
    echo "❌ Shared JVM tests failed."
    exit 1
fi
echo "✅ Shared JVM tests passed."

# 3. Android UI Tests (Robolectric)
echo "-----------------------------------------"
echo "3. Running Android UI Tests (Robolectric)"
./gradlew :androidApp:testDebugUnitTest
if [ $? -ne 0 ]; then
    echo "❌ Android UI tests failed."
    exit 1
fi
echo "✅ Android UI tests passed."

echo "========================================="
echo "  🎉 All Regression Checks Passed!       "
echo "========================================="
exit 0
