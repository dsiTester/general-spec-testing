if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

NUM_THREADS=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
# BASE_DIR=$( dirname ${SCRIPT_DIR} )
LOGS_DIR=${SCRIPT_DIR}/limit-study-logs
mkdir -p ${LOGS_DIR}
OVERALL=${LOGS_DIR}/gol-summary

if [ ! -d ${SCRIPT_DIR}/limit-study-pml-files ]; then
    echo "Unpacking ${SCRIPT_DIR}/limit-study-pml-files.tgz"
    (
	cd ${SCRIPT_DIR}
	tar xf limit-study-pml-files.tgz
    )
fi

echo "LIMIT STUDY STARTED AT: "`date +%Y-%m-%d-%H-%M-%S`
bash ${SCRIPT_DIR}/run-batch.sh ${SCRIPT_DIR}/limit-study.txt ${SCRIPT_DIR}/limit-study-pml-files ${NUM_THREADS}

echo "LIMIT STUDY ENDED AT: "`date +%Y-%m-%d-%H-%M-%S`
