# script that should be called via GNU parallel

if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 AUTOMATON_DIR"
    exit
fi

AUTOMATON_DIR=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

MODEL_CHECKING_DIR=${SCRIPT_DIR}/spec-exploration/scripts/model-checking
NBP_CHECKER_DIR=${SCRIPT_DIR}/nbp-checker/nbp
STATE_CHECKER_DIR=${SCRIPT_DIR}/state-comparison/scripts
DSI_DIR=${SCRIPT_DIR}/dsi/scripts

AUTOMATON_NAME=$( echo ${AUTOMATON_DIR} | rev | cut -d/ -f1 | rev )
MINER=$( echo "${AUTOMATON_NAME}" | cut -d@ -f1 )
PROJECT=$( echo "${AUTOMATON_NAME}" | cut -d@ -f2 )
PROJECT_FILES_DIR=${SCRIPT_DIR}/project-files
PROJECT_FILE=${PROJECT_FILES_DIR}/${PROJECT}.txt
mkdir -p ${PROJECT_FILES_DIR}

GENERATED_DATA_DIR=${SCRIPT_DIR}/data/generated-data
LOGS_DIR=${GENERATED_DATA_DIR}/${AUTOMATON_NAME}/logs
if [ -d ${LOGS_DIR} ]; then
    mv ${GENERATED_DATA_DIR} ${GENERATED_DATA_DIR}-`date +%Y-%m-%d-%H-%M-%S`
fi
mkdir -p ${LOGS_DIR}

function run_components() {
    wrapper_log=${LOGS_DIR}/gol-run-info
    echo START TIME: `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    echo =====EXTRACTING CONSTRAINTS started at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    ( time timeout 2h bash ${MODEL_CHECKING_DIR}/run-pml.sh ${AUTOMATON_DIR}/${AUTOMATON_NAME}.pml -no ) &> ${LOGS_DIR}/gol-run-pml
    ( time bash ${MODEL_CHECKING_DIR}/analyze-pml-output.sh ${MODEL_CHECKING_DIR}/data/generated-data/ ${AUTOMATON_NAME} ${AUTOMATON_DIR} ) &> ${LOGS_DIR}/gol-analyze-pml-output
    ( time bash ${MODEL_CHECKING_DIR}/create-inputs-for-spec-testing.sh ${MODEL_CHECKING_DIR}/data/generated-data -ALL ${AUTOMATON_NAME} ) &> ${LOGS_DIR}/gol-create-inputs-for-spec-testing
    created_smap=${MODEL_CHECKING_DIR}/data/generated-data/new-sMaps/${AUTOMATON_NAME}-sMap.txt
    echo =====EXTRACTING CONSTRAINTS ended at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    if [ ! -f ${created_smap} ]; then
        echo "initial smap not created!" >> ${wrapper_log}
        exit
    fi

    echo =====RUNNING NBP CHECKER started at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    ( time bash ${NBP_CHECKER_DIR}/run-nbp-checker.sh ${AUTOMATON_NAME} ${PROJECT_FILE} ${created_smap} ) &> ${LOGS_DIR}/gol-run-nbp-checker
    post_nbp_checker_smap=${NBP_CHECKER_DIR}/${AUTOMATON_NAME}/nbp-filtered-smap-${AUTOMATON_NAME}.txt
    echo =====RUNNING NBP CHECKER ended at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    if [ ! -f ${post_nbp_checker_smap} ]; then
	echo "post-nbp-checker smap not created!" >> ${wrapper_log}
	exit
    fi

    echo =====RUNNING STATE CHECKER started at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    ( time bash ${STATE_CHECKER_DIR}/sequential-state-checker.sh ${AUTOMATON_NAME} ${PROJECT} ${post_nbp_checker_smap} ) &> ${LOGS_DIR}/gol-run-state-checker
    post_state_checker_smap=${STATE_CHECKER_DIR}/data/generated-data/${AUTOMATON_NAME}/results/${AUTOMATON_NAME}-remaining-specs-sMap.txt
    echo =====RUNNING STATE CHECKER ended at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    if [ ! -f ${post_state_checker_smap} ]; then
	echo "post-state-checker smap not created!" >> ${wrapper_log}
	exit
    fi

    echo =====RUNNING DSI started at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
    ( time bash ${DSI_DIR}/run-dsi-component-sequential.sh ${AUTOMATON_NAME} ${PROJECT_FILE} ${post_state_checker_smap} ) &> ${LOGS_DIR}/gol-run-dsi
    echo =====RUNNING DSI ended at `date +%Y-%m-%d-%H-%M-%S` >> ${wrapper_log}
}

echo "=====RUNNING AUTOMATON ${AUTOMATON_NAME}"

run_components
