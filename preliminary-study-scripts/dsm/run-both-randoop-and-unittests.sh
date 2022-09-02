if [ $# -ne 5 ]; then
    echo "USAGE: $0 PROJECT_NAME RANDOOP_TRACES MANUAL_TRACES DSM_DIR NUM_THREADS"
fi


project=$1
randoop=$2
manual=$3
dsm_dir=$4
num_threads=$5

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

DIR=${SCRIPT_DIR}/${project}-both-randoop-and-unittests/
GENERAL_LOGS_DIR=${SCRIPT_DIR}/both-randoop-and-unittests-logs
RUN_INFO_FILE=${GENERAL_LOGS_DIR}/gol-randoop-unittests-${project}
TRACES_DIR=${DIR}/traces

mkdir -p ${DIR}
mkdir -p ${GENERAL_LOGS_DIR}
mkdir -p ${TRACES_DIR}

echo "run-both-randoop-and-unittests: moving randoop traces start `date +%Y-%m-%d-%H-%M-%S`"  | tee -a ${RUN_INFO_FILE}
ls ${randoop} | xargs -I {} cp ${randoop}/{} ${TRACES_DIR}
echo "run-both-randoop-and-unittests: moving randoop traces end `date +%Y-%m-%d-%H-%M-%S`"  | tee -a ${RUN_INFO_FILE}
echo "run-both-randoop-and-unittests: moving dev unit test traces start `date +%Y-%m-%d-%H-%M-%S`"  | tee -a ${RUN_INFO_FILE}
ls ${manual} | xargs -I {} cp ${manual}/{} ${TRACES_DIR}
echo "run-both-randoop-and-unittests: moving dev unit test traces end `date +%Y-%m-%d-%H-%M-%S`"  | tee -a ${RUN_INFO_FILE}

( time bash run-dsm.sh ${DIR}/ws ${dsm_dir} ${DIR}/${project}-specs ${TRACES_DIR} ${num_threads} ) &> ${RUN_INFO_FILE}
