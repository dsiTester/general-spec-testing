if [ $# != 2 ]; then
    echo "USAGE: $0 project-name num-threads"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
REPO_DIR=$( dirname "${SCRIPT_DIR}" )
PROJECT=$1
NUM_THREADS=$2
LOGS_DIR=logs/${PROJECT}
mkdir -p ${LOGS_DIR}
SUMMARY_LOG=${LOGS_DIR}/gol-summary
TEXADA_TRACES_DIR=${SCRIPT_DIR}/texada-traces


if [ ! -d ${TEXADA_TRACES_DIR} ]; then
    (
        cd ${SCRIPT_DIR}
        tar xf texada-traces.tgz
    )
fi

if [ ! -f ${TEXADA_TRACES_DIR}/${PROJECT}-texada-traces.txt ]; then
    (
        cd ${TEXADA_TRACES_DIR}
        tar xf ${PROJECT}-texada-traces.txt.tgz
    )
fi

echo "${PROJECT} start time: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${SUMMARY_LOG}

(
    export LD_LIBRARY_PATH # =/usr/local/lib/libspot.so.0
    export LOGS_DIR
    export SUMMARY_LOG
    export TEXADA_TRACES_DIR
    export PROJECT
    find -name args.txt | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/run-texada-on-proj-parallel.sh
)

echo "${PROJECT} end time: `date +%Y-%m-%d-%H-%M-%S`"  | tee -a ${SUMMARY_LOG}
