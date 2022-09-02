if [ $# -ne 2 ]; then
    echo "USAGE: $0 PROJECT NUM_THREADS"
    exit
fi

PROJECT=$1
NUM_THREADS=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
SETUP_FILE=${DATA_DIR}/initial-experiments/${PROJECT}-setup.csv

TMP_LOG=${SCRIPT_DIR}/tmp-log

echo "[WRAPPER] Running experiments for project ${PROJECT} start: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${TMP_LOG}"
bash ${SCRIPT_DIR}/run-aspectj-set-get.sh ${PROJECT} ${NUM_THREADS}
echo "[WRAPPER] Running experiments for project ${PROJECT} end: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${TMP_LOG}"

PROJECT_DIR=${SCRIPT_DIR}/data/generated-data/${PROJECT}
IN=${PROJECT_DIR}/logs
OUT=${PROJECT_DIR}/results
mkdir -p ${OUT}
LOG="${OUT}/gol-process-results"
mv ${TMP_LOG} ${LOG}

echo "[WRAPPER] Processing verdict info for project ${PROJECT} start: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${LOG}"
bash ${SCRIPT_DIR}/process-aspectj-set-gets.sh ${PROJECT} ${IN} ${OUT}
echo "[WRAPPER] Processing verdict info for project ${PROJECT} end: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${LOG}"

echo "[WRAPPER] Processing timing info for project ${PROJECT} start: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${LOG}"
bash ${SCRIPT_DIR}/compare-state-time-to-dsi-time.sh ${PROJECT} ${PROJECT_DIR} ${OUT}
echo "[WRAPPER] Processing timing info for project ${PROJECT} end: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${LOG}"

echo "[WRAPPER] Analyzing verdict info for project ${PROJECT} start: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${LOG}"
python3 ${SCRIPT_DIR}/analyze-checked-output.py ${OUT}/${PROJECT}-results.csv ${OUT} ${PROJECT} ${DATA_DIR}/initial-experiments/manual-categorizations-per-project.csv
echo "[WRAPPER] Analyzing verdict info for project ${PROJECT} end: `date +%Y-%m-%d-%H-%M-%S`" | tee -a "${LOG}"
