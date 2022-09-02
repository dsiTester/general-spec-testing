#!/bin/sh

if [ $# -lt 1 ]; then
    echo "Usage: $0 CONFIG"
    exit
fi

CONFIG=$1

TEST_CLASS_NAME=$( echo "${CONFIG}" | cut -d, -f1 )
MODE_VERIFY=$( echo "${CONFIG}" | cut -d, -f2 )
SELECTION_OPTION=$( echo "${CONFIG}" | cut -d',' -f3 )

project_location=`dirname ${PWD}`
comp_log_file=${project_location}/logs/gol-compile-${TEST_CLASS_NAME}
mining_log_file=${project_location}/logs/gol-mine-test-classes-${TEST_CLASS_NAME}
log_file=${project_location}/logs/gol-test-classes-${TEST_CLASS_NAME}
mkdir -p ${project_location}/logs/
methodnames=${project_location}/ws/dsi-target/testnames.txt
SPEC_FILE=${project_location}/ws/gol/surefire-reports/${TEST_CLASS_NAME}/spec-order.txt
TESTCLASS_SPEC_FILE=${project_location}/../testClasses/surefire-reports/${TEST_CLASS_NAME}/spec-order.txt # to avoid mining for cross gran if we already have the original

mkdir -p ${project_location}/ws/gol/surefire-reports/${TEST_CLASS_NAME}

function collect() {
    time mvn dsi:collect -Dtest=${TEST_CLASS_NAME} -DbuildDirectory=${TEST_CLASS_NAME} -DtempDir=${TEST_CLASS_NAME} ${SKIPS}
    echo -e "\nDSI:COLLECT DONE!!!!\n"
}

function run_verify() {
    local option=$1
    echo -e "\n****************run_verify with option = ${option}\n"
    time mvn dsi:run-dsi ${option} -DspecFile=${SPEC_FILE} -DbuildDirectory=${TEST_CLASS_NAME} -DtempDir=${TEST_CLASS_NAME} -DtestSpec=${TEST_CLASS_NAME} -Dselection=${SELECTION_OPTION} ${SKIPS}
}

function main() {
    echo "starting to run ${CONFIG}..."
    echo "######################################################################" > ${log_file}
    echo "RUNNING ${TEST_CLASS_NAME}..." >> ${log_file}
    echo "######################################################################" >> ${log_file}
    export MAVEN_OPTS="-Xmx30G -Xss16G -XX:MaxPermSize=28GM"

    # test compile here for the specific build dir
    ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${TEST_CLASS_NAME} ) >> ${comp_log_file} 2>&1

    # check if test method spec order file exists
    if [ -f "${TESTCLASS_SPEC_FILE}" ]; then
        echo "test class spec order exists!! not mining." | tee -a ${mining_log_file}
        cp ${TESTCLASS_SPEC_FILE} ${SPEC_FILE}
    else
        echo "no spec order exists... mining." | tee -a ${mining_log_file}
        # mine on this test class once before running dsi...
        ( time mvn dsi:mine ${COMP_SKIPS} -Dtest=${TEST_CLASS_NAME} -DbuildDirectory=${TEST_CLASS_NAME} ) &> ${mining_log_file}
    fi

    if [ "${MODE_VERIFY}" == "testClasses" ]; then
        echo "Verifying with testClasses..."
	( time mvn dsi:run-dsi ${SKIPS} -DtempDir=${TEST_CLASS_NAME} -Dtest=${TEST_CLASS_NAME} -DbuildDirectory=${TEST_CLASS_NAME} -DspecFile=${SPEC_FILE} -Dselection=${SELECTION_OPTION} ) >> ${log_file} 2>&1
    elif  [ "${MODE_VERIFY}" == "allTests" ]; then
        echo "Verifying with allTests..."
	option=""
        # ( collect )  >> ${log_file} 2>&1
        ( run_verify "${option}" )  >> ${log_file} 2>&1
    elif [ "${MODE_VERIFY}" == "testMethods" ]; then
        # ( collect )  >> ${log_file} 2>&1
        # need to loop because we probably shouldn't do parallel on top of parallel...
        for TEST_METHOD_NAME in $( cat ${methodnames} ); do
            option="-Dtest=${TEST_METHOD_NAME}"
            ( run_verify "${option}" )  >> ${log_file} 2>&1
        done
    fi
}

main
