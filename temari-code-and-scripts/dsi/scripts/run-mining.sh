SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
project_location=`dirname ${PWD}`

TEST_NAME=$1

mining_log_file=${project_location}/logs/gol-mine-${TEST_NAME}
comp_log_file=${project_location}/logs/gol-compile-${TEST_NAME}
buildDirectoryName=$( echo "${TEST_NAME}" | sed 's/#/-/g' ) # antlr will not work if we have a # in the directory name

if [ "${TEST_NAME}" != "all-tests" ]; then
    op="-Dtest=${TEST_NAME}"
else
    op=""
fi

echo "MINING WITH TEST ${TEST_NAME}"

# ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${buildDirectoryName} -DtempDir=${buildDirectoryName} ) >> ${comp_log_file} 2>&1
# copying over the originally compiled target directory to avoid extra work...
cp -r target ${buildDirectoryName}

( time mvn dsi:mine ${SKIPS} -DbuildDirectory=${buildDirectoryName} -DtempDir=${buildDirectoryName} -DoutputPathString=dsi-target/mined-specs-${TEST_NAME}.txt -DrawSpecOutputPathString=dsi-target/raw-specs-${TEST_NAME}.txt -DspecOutputOp="write-all-to-file" ${op} ) &> ${mining_log_file}

# delete to save space
rm -rf ${buildDirectoryName}
