if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 PROJECT NUM_THREADS"
    exit
fi

PROJECT=$1
NUM_THREADS=$2
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
TRACES_DIR=${SCRIPT_DIR}/dsm-randoop-traces

GENERAL_LOGS_DIR=${SCRIPT_DIR}/dsm-randoop-logs
RUN_INFO_FILE=${GENERAL_LOGS_DIR}/gol-dsm-randoop-${PROJECT}
mkdir -p ${GENERAL_LOGS_DIR}
WS=${SCRIPT_DIR}/dsm-randoop-workspace/${PROJECT}
mkdir -p ${WS}

if [ ! -d ${TRACES_DIR}/${PROJECT} ]; then
    echo "Unpacking ${TRACES_DIR}/${PROJECT}.tgz"
    (
        cd ${TRACES_DIR}
        tar xf ${PROJECT}.tgz
    )
fi

echo "run-dsm.sh: converting to DSM trace started at `date +%Y-%m-%d-%H-%M-%S`"
( time python3 ${SCRIPT_DIR}/convert-dsi-trace-to-dsm-trace.py ${TRACES_DIR}/${PROJECT} ${WS} ) &> ${GENERAL_LOGS_DIR}/gol-conversion
echo "run-dsm.sh: converting to DSM trace ended at `date +%Y-%m-%d-%H-%M-%S`"


( set -o xtrace ; time bash ${SCRIPT_DIR}/run-dsm.sh ${WS} ${SCRIPT_DIR}/dsm-randoop-${PROJECT}-specs ${NUM_THREADS} ; set +o xtrace ) &> ${RUN_INFO_FILE}
