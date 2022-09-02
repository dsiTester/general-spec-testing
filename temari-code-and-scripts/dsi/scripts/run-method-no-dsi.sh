#!/bin/sh

if [ $# != 1 ]; then
    echo "usage: bash $0 TEST_METHOD_NAME"
    exit
fi

TEST_METHOD_NAME=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
project_location=`dirname ${PWD}`
comp_log_file=${project_location}/logs/gol-compile-${TEST_METHOD_NAME}
log_file=${project_location}/logs/gol-no-dsi-test-method-${TEST_METHOD_NAME}
COMP_SKIPS=" -Dcheckstyle.skip -Drat.skip -Denforcer.skip -Danimal.sniffer.skip -Dmaven.javadoc.skip -Dfindbugs.skip -Dwarbucks.skip -Dmodernizer.skip -Dimpsort.skip -DfailIfNoTests=false"
SKIPS="${COMP_SKIPS} -Dmaven.main.skip"

if [[ ${TEST_METHOD_NAME} != *\#* ]]; then
    echo "${TEST_METHOD_NAME}: NOT A TEST METHOD!! Skipping..."
    exit
fi

echo "RUNNING ${TEST_METHOD_NAME}..."

# invoke your code on ${TEST_METHOD_NAME}
echo "#####################################################" > ${log_file}
echo "RUNNING ${TEST_METHOD_NAME}..." >> ${log_file}
echo "#####################################################" >> ${log_file}
export MAVEN_OPTS="-Xmx30G -Xss16G -XX:MaxPermSize=28GM"

# ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${TEST_METHOD_NAME} ) >> ${comp_log_file} 2>&1
# ( time mvn surefire:test ${SKIPS} -DbuildDirectory=${TEST_METHOD_NAME} -DtempDir=${TEST_METHOD_NAME} -Dtest=${TEST_METHOD_NAME} ) >> ${log_file} 2>&1
( time mvn surefire:test ${SKIPS} -DtempDir=${TEST_METHOD_NAME} -Dtest=${TEST_METHOD_NAME} ) >> ${log_file} 2>&1
