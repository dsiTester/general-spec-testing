TEST_NAME=$1
SHORT_TEST_NAME=$( echo "${TEST_NAME}" | rev | cut -d. -f1 | rev )

SUMMARY_LOG=${LOGS_DIR}/gol-summary-${TEST_NAME}
echo "Atomic run of test ${TEST_NAME} STARTED AT `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${SUMMARY_LOG}
EVOSUITE_RUN_1_LOG=${LOGS_DIR}/gol-run-evosuite-1-${TEST_NAME}
EVOSUITE_RUN_2_LOG=${LOGS_DIR}/gol-run-evosuite-2-${TEST_NAME}
LTL_RUN_LOG=${LOGS_DIR}/gol-run-ltl-${TEST_NAME}
DICE_MAIN_RUN_LOG=${LOGS_DIR}/gol-run-dicemain-${TEST_NAME}

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

# DICE_TESTER_DIR=${WS}/dice-tester-${TEST_NAME}
DICE_MINER_DIR=${WS}/dice-miner-${TEST_NAME}
cp -r ${OG_DICE_MINER_DIR} ${DICE_MINER_DIR}

WS1_DIR=${WS}/evosuite-workspace-1-${TEST_NAME}
WS2_DIR=${WS}/evosuite-workspace-2-${TEST_NAME}
RESULTS_DIR=${PROJECT_DATA_DIR}/results/${TEST_NAME}

mkdir -p ${RESULTS_DIR}
mkdir ${WS1_DIR}
mkdir ${WS2_DIR}

# perform first run of evosuite
cd ${WS1_DIR}
mkdir evosuite-tests
touch NOW
echo "EVOSUITE RUN 1 STARTED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}

( set -o xtrace ; time timeout 30s java -jar ${DICE_TESTER_JAR} -class ${TEST_NAME} -projectCP ${PROJECT_CLASSPATH} -Dsearch_budget=10 ; set +o xtrace ) &> ${EVOSUITE_RUN_1_LOG}

echo "EVOSUITE RUN 1 ENDED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}

find -newer NOW >> ${EVOSUITE_RUN_1_LOG}

if [ ! -f evosuite-tests/${SHORT_TEST_NAME}.pures ]; then
    echo "Patching ${SHORT_TEST_NAME}.pures" >> ${EVOSUITE_RUN_1_LOG}
    > evosuite-tests/${SHORT_TEST_NAME}.pures
fi

# mine the LTL

cd ${DICE_MINER_DIR}

VOCAB_FILE=${TEST_NAME}.vocab.txt
LTL_OUT_FILE=ltl_${TEST_NAME}.txt

echo "LTL RUN STARTED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}
( set -o xtrace; time timeout 3h python3 ltl_rules.py ${OG_TRACES_DIR}/${TEST_NAME}.txt ${WS1_DIR}/evosuite-tests/${SHORT_TEST_NAME}.pures ${VOCAB_FILE} | grep "LTL" > ${LTL_OUT_FILE} ; set +o xtrace ) &> ${LTL_RUN_LOG}
echo "LTL RUN ENDED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}

# exit early if we don't have any LTL formulae (timed out), since we don't have anything to generate counterexamples from
if ! grep -q "LTL" ${LTL_RUN_LOG} ; then
    echo "No LTL formulae! (Potentially timed out)" >> ${LTL_RUN_LOG}
    echo "Exiting early for having no LTL formulae" >> ${SUMMARY_LOG}
    exit
fi

mkdir -p dice_tester_traces

cp ${VOCAB_FILE} ${RESULTS_DIR}
cp ${LTL_OUT_FILE} ${RESULTS_DIR}
mv ${VOCAB_FILE} ${WS2_DIR}
mv ${LTL_OUT_FILE} ${WS2_DIR}

# perform second run of evosuite
cd ${WS2_DIR}
touch NOW

echo "EVOSUITE RUN 2 STARTED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}
echo "path:${WS2_DIR}/${LTL_OUT_FILE}" > config.txt 
( set -o xtrace ; time timeout 1000s java -jar ${DICE_TESTER_JAR} -class ${TEST_NAME} -projectCP ${PROJECT_CLASSPATH} -Dsearch_budget=10 ; set +o xtrace ) &> ${EVOSUITE_RUN_2_LOG}

echo "EVOSUITE RUN 2 ENDED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}

find -newer NOW >> ${EVOSUITE_RUN_2_LOG}

ENHANCED_TRACES_FILE=${DICE_MINER_DIR}/dice_tester_traces/${TEST_NAME}_enhanced.traces
if [ -f null_${TEST_NAME}.traces ]; then
    cp null_${TEST_NAME}.traces ${ENHANCED_TRACES_FILE}
else
    echo "Patching ${ENHANCED_TRACES_FILE}" >> ${EVOSUITE_RUN_2_LOG}
    > ${ENHANCED_TRACES_FILE}
fi
cp ${ENHANCED_TRACES_FILE} ${RESULTS_DIR}

# run main dice given all of this
cd ${DICE_MINER_DIR}

mkdir -p ${DICE_MINER_DIR}/outputs
DICE_MAIN_OUT_DIR=${DICE_MINER_DIR}/outputs/${TEST_NAME}/
echo "DICE RUN MAIN STARTED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}

sed -i "s/<init>/${SHORT_TEST_NAME}/g" dice_tester_traces/${TEST_NAME}_enhanced.traces

( set -o xtrace ; time timeout 3h python3 main.py ${OG_TRACES_DIR}/${TEST_NAME}.txt ${WS1_DIR}/evosuite-tests/${SHORT_TEST_NAME}.pures ${ENHANCED_TRACES_FILE} ${DICE_MAIN_OUT_DIR} 3000; set +o xtrace ) &> ${DICE_MAIN_RUN_LOG}

cp -r ${DICE_MAIN_OUT_DIR} ${RESULTS_DIR}/dice-main

echo "DICE RUN MAIN ENDED AT `date +%Y-%m-%d-%H-%M-%S`" >> ${SUMMARY_LOG}

echo "Atomic run of test ${TEST_NAME} ENDED AT `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${SUMMARY_LOG}

rm -rf ${DICE_MINER_DIR}
