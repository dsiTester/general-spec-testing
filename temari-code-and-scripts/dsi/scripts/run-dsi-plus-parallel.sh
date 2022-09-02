#!/bin/sh
# invoked by run-dsi-plus.sh to run DSI+ on tests in parallel

if [ $# != 1 ]; then
    echo "usage: bash $0 CONFIG"
    exit
fi

CONFIG=$1
TEST=$( echo "${CONFIG}" | cut -d'@' -f1 )
MODE=$( echo "${CONFIG}" | cut -d'@' -f2 )
MINING_MODE=$( echo "${CONFIG}" | cut -d'@' -f3 )
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
project_location=`dirname ${PWD}`
comp_log_file=${project_location}/logs/gol-compile-${TEST}
log_file=${project_location}/logs/gol-dsi-plus-${TEST}
all_configs=${project_location}/logs/dsi-plus-all-configs.txt
test_index=$( grep -n "^${TEST}$" ${all_configs} | cut -d: -f1 )
total_num_tests=$( cat ${all_configs} | wc -l )
buildDirectoryName=$( echo "${TEST}" | sed 's/#/-/g' ) # antlr will not work if we have a # in the directory name
tMap=test-to-spec-maps/${TEST}-specs.txt

if [ "${TEST}" == "all-tests" ]; then
    op=""
else
    op="-Dtest=${TEST}"
fi

if [ "${MINING_MODE}" == "-noMining" ]; then
    spec_file_op="-DspecFile=${tMap}"
else
    spec_file_op=""
fi

echo "RUNNING ${MODE} WITH TEST ${TEST} (${test_index} out of total ${total_num_tests} test configs)"

# uncomment to test compile for the specific build dir
# ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${buildDirectoryName} -DtempDir=${buildDirectoryName} ) >> ${comp_log_file} 2>&1

# copying over the originally compiled target directory to avoid extra work...
cp -r target ${buildDirectoryName}

( time mvn dsi:run-dsi-plus ${SKIPS} ${op} ${spec_file_op} -DbuildDirectory=${buildDirectoryName} -DtempDir=${buildDirectoryName} -DwriteSpecsToSurefireReport=true -DspecOutputOp="write-all-to-file" -DdsiVersionOp=${MODE} ) >> ${log_file} 2>&1

# delete to save space
rm -rf ${buildDirectoryName}
