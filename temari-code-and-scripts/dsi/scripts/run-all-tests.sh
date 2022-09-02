#!/bin/sh

if [ $# -ne 1 ]; then
    echo "Usage: $0 TEST_NAME"
    echo "This script should only be run from the run.sh script"
    exit
fi

CONFIG=$1
TEST_NAME=$( echo "${CONFIG}" | cut -d',' -f1 )
SELECTION_OPTION=$( echo "${CONFIG}" | cut -d',' -f2 )

# This script will only be run in the ws directory of the currently evaluated project.
project_location=`dirname ${PWD}`
comp_log_file=${project_location}/logs/gol-compile-all-tests@${TEST_NAME}
log_file=${project_location}/logs/gol-all-tests@${TEST_NAME}
mkdir -p ${project_location}/logs/
DEFAULT_BUILD_DIR_NAME=target
SPEC_FILE=${project_location}/ws/gol/surefire-reports/all-tests/spec-order.txt

function run_verify() {
    local test_name=all-tests
    echo -e "\n****************run_verify with test name = ${test_name}\n"
    time mvn dsi:run-dsi -Dtest=${TEST_NAME} -DspecFile=${SPEC_FILE} -DbuildDirectory=${TEST_NAME} -DtestSpec=${test_name} -DtempDir=${TEST_NAME} -Dselection=${SELECTION_OPTION} ${SKIPS}
}

function main() {
    echo "######################################################################" > ${log_file}
    echo "RUNNING ${TEST_NAME}..." >> ${log_file}
    echo "######################################################################" >> ${log_file}
    export MAVEN_OPTS="-Xmx30G -Xss16G -XX:MaxPermSize=28GM"

    # test compile here for the specific build dir
    ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${TEST_NAME} ) >> ${comp_log_file} 2>&1
    ( run_verify )  >> ${log_file} 2>&1
}

main
