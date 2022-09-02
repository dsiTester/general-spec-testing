if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 PROJECT NUM_THREADS"
    exit
fi

PROJECT=$1
NUM_THREADS=$2
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
MANUAL_TRACES_DIR=${BASE_DIR}/dice-dsm-manual-traces

GENERAL_LOGS_DIR=${SCRIPT_DIR}/dsm-manual-logs
RUN_INFO_FILE=${GENERAL_LOGS_DIR}/gol-dsm-manual-${PROJECT}
mkdir -p ${GENERAL_LOGS_DIR}
WS=${SCRIPT_DIR}/dsm-manual-workspace/${PROJECT}
mkdir -p ${WS}

# open up the traces directory if we haven't already done so
if [ ! -d ${MANUAL_TRACES_DIR} ]; then
    echo "Unpacking ${MANUAL_TRACES_DIR}.tgz"
    (
        cd ${BASE_DIR}
        tar xf dice-dsm-manual-traces.tgz
    )
fi

if [ ! -d ${MANUAL_TRACES_DIR}/${PROJECT} ]; then
    echo "Unpacking ${MANUAL_TRACES_DIR}/${PROJECT}.tgz"
    (
        cd ${MANUAL_TRACES_DIR}
        tar xf ${PROJECT}.tgz
    )
fi

echo "Reorganizing contents of ${MANUAL_TRACES_DIR}/${PROJECT}"
for f in $( ls ${MANUAL_TRACES_DIR}/${PROJECT} ); do
    name=$( echo ${f} | rev | cut -d. -f2- | rev )
    if [ ! -d ${WS}/${name}/input_traces ]; then
	mkdir -p ${WS}/${name}/input_traces
	cp ${MANUAL_TRACES_DIR}/${PROJECT}/${f} ${WS}/${name}/input_traces/input.txt
    fi
done

( set -o xtrace ; time bash ${SCRIPT_DIR}/run-dsm.sh ${WS} ${SCRIPT_DIR}/dsm-manual-${PROJECT}-specs ${NUM_THREADS} ; set +o xtrace ) &> ${RUN_INFO_FILE}
