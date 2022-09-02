#!/bin/sh

if [ $# != 1 ]; then
    echo "usage: bash $0 CONFIG"
    exit
fi

CONFIG=$1

TEST_METHOD_NAME=$( echo "${CONFIG}" | cut -d, -f1 )
MODE_VERIFY=$( echo "${CONFIG}" | cut -d, -f2 )
SELECTION_OPTION=$( echo "${CONFIG}" | cut -d',' -f3 )
TEST_CLASS_NAME=$( echo ${TEST_METHOD_NAME} | cut -d'#' -f1 )

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
project_location=`dirname ${PWD}`

if [ "${MODE_VERIFY}" == "testClasses" ]; then
    LOG_TEST_NAME="${TEST_METHOD_NAME}@${TEST_CLASS_NAME}"
elif [ "${MODE_VERIFY}" == "allTests" ]; then
    LOG_TEST_NAME="${TEST_METHOD_NAME}@all-tests"
else
    LOG_TEST_NAME="${TEST_METHOD_NAME}@${TEST_METHOD_NAME}"
fi

echo ${LOG_TEST_NAME}

mining_log_file=${project_location}/logs/gol-mine-test-method-${TEST_METHOD_NAME}
comp_log_file=${project_location}/logs/gol-compile-${LOG_TEST_NAME}
log_file=${project_location}/logs/gol-test-method-${LOG_TEST_NAME}
SPEC_FILE=${project_location}/ws/gol/surefire-reports/${LOG_TEST_NAME}/spec-order.txt
TESTMETHOD_SPEC_FILE=${project_location}/../testMethods/surefire-reports/${TEST_METHOD_NAME}@${TEST_METHOD_NAME}/spec-order.txt # to avoid mining for cross gran if we already have the original

if [[ ${TEST_METHOD_NAME} != *\#* ]]; then
    echo "${TEST_METHOD_NAME}: NOT A TEST METHOD!! Skipping..."
    exit
fi

function run_verify() {
    local option=$1
    time mvn dsi:run-dsi ${option} -DspecFile=${SPEC_FILE} -DbuildDirectory=${TEST_METHOD_NAME} -DtempDir=${TEST_METHOD_NAME} -DtestSpec=${TEST_METHOD_NAME} -Dselection=${SELECTION_OPTION} ${SKIPS}
}

function main() {

    echo "RUNNING ${CONFIG}..."

    echo "#####################################################" > ${log_file}
    echo "RUNNING ${CONFIG}..." >> ${log_file}
    echo "#####################################################" >> ${log_file}

    # test compile here for the specific build dir
    ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${TEST_METHOD_NAME} -DtempDir=${TEST_METHOD_NAME} ) >> ${comp_log_file} 2>&1

    echo "${TESTMETHOD_SPEC_FILE}"
    # check if test method spec order file exists
    if [ -f "${TESTMETHOD_SPEC_FILE}" ]; then
        echo "test method spec order exists!! not mining." | tee -a ${mining_log_file}
        cp ${TESTMETHOD_SPEC_FILE} ${SPEC_FILE}
    else
        # mine on this test method once before running dsi...
        echo "no method spec order exists... mining." | tee -a ${mining_log_file}
        ( time mvn dsi:mine ${COMP_SKIPS} -DbuildDirectory=${TEST_METHOD_NAME} -Dtest=${TEST_METHOD_NAME} -DoutputDirectoryName=${LOG_TEST_NAME} ) &> ${mining_log_file}
    fi

    if [ "${MODE_VERIFY}" == "testMethods" ]; then
        echo "Verifying with testMethods..."
        ( time mvn dsi:run-dsi -DbuildDirectory=${TEST_METHOD_NAME} -DtempDir=${TEST_METHOD_NAME} -Dtest=${TEST_METHOD_NAME} -DspecFile=${SPEC_FILE} -Dselection=${SELECTION_OPTION} ${SKIPS} ) >> ${log_file} 2>&1
    elif [ "${MODE_VERIFY}" == "testClasses" ]; then
        echo "Verifying with testClasses..."
        option="-Dtest=${TEST_CLASS_NAME}"
        ( run_verify "${option}" )  >> ${log_file} 2>&1
    elif [ "${MODE_VERIFY}" == "allTests" ]; then
        echo "Verifying with allTests..."
        option=""
        ( run_verify "${option}" )  >> ${log_file} 2>&1
    else
        echo "ERROR!!! MODE_VERIFY is invalid: ${MODE_VERIFY}"
    fi

}

main
