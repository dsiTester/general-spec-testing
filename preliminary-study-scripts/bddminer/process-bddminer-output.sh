if [ $# -lt 4 ]; then
    echo "USAGE: bash $0 PROJECT_NAME BDDMINER_OUTPUT_DIR SPEC_TO_AUTOMATA_DIR NUM_THREADS [NUM_LETTERS_OPT]"
    echo "where [NUM_LETTERS_OPT] should be set to 3 if dealing with three-letter specs"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
PROJECT=$1
BDDMINER_OUTPUT_DIR=$2
IN_DIR=${BDDMINER_OUTPUT_DIR}/${PROJECT}
SPEC_TO_AUTOMATA_DIR=$3
NUM_THREADS=$4
LOGICREPO_PATH=${SPEC_TO_AUTOMATA_DIR}/logicrepository
WS=${SCRIPT_DIR}/${PROJECT}-ws
WS_NAME=${PROJECT}-ws
NUM_LETTERS_OPT=$5
if [ "${NUM_LETTERS_OPT}" == "3" ]; then
    echo "PROCESSING THREE-LETTER BDDMINER SPECS"
    OUT=${SCRIPT_DIR}/bdd-3-fsms/${PROJECT}
else
    echo "PROCESSING TWO-LETTER BDDMINER SPECS"
    NUM_LETTERS_OPT=2
    OUT=${SCRIPT_DIR}/bdd-2-fsms/${PROJECT}
fi
LOGS_OUT=${SCRIPT_DIR}/logs/${NUM_LETTERS_OPT}-letter-${PROJECT}
mkdir -p ${OUT}
mkdir -p ${LOGS_OUT}

TIMING_LOG_FILE=${LOGS_OUT}/gol-timings

function preprocess() {
    echo "start preprocessing: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
    ( time python3 ${SCRIPT_DIR}/preprocess-bddminer-output.py ${IN_DIR} ${WS} ${PROJECT} ) &> ${LOGS_OUT}/gol-preprocess
    echo "finish preprocessing: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
}

function create_automata() {
    # package only once
    mvn package &> /dev/null
    echo "start creating automata: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
    (
	export SCRIPT_DIR
	export SPEC_TO_AUTOMATA_DIR
	export WS
	export PROJECT
	export LOGICREPO_PATH
	export LOGS_OUT
	export OUT
	ls ${WS} | grep -v spec-id-to-mined-tests.csv | rev | cut -d- -f2 | rev | sort -u | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/parallel-process-bddminer-output-helper.sh
    )
    echo "finish creating automata: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
}

preprocess
create_automata
cp ${WS}/spec-id-to-mined-tests.csv ${OUT}/
echo "start packaging workspace: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
cd ${SCRIPT_DIR}
tar -czf ${WS_NAME}.tgz ${WS_NAME}
rm -rf ${WS}
echo "finish packaging workspace: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}

OUT_PARENT=$( dirname ${OUT} )
cd ${OUT_PARENT}
echo "start packaging results: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
tar -czf ${PROJECT}.tgz ${PROJECT}
rm -rf ${PROJECT}
echo "finish packaging results: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMING_LOG_FILE}
