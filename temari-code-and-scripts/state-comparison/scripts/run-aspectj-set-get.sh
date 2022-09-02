if [ $# -ne 2 ]; then
    echo "USAGE: $0 PROJECT NUM_THREADS"
    exit
fi

PROJECT=$1
NUM_THREADS=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
CONVERSION_DIR=${BASE_DIR}/spec-to-aspect
CONVERSION_JAR=${CONVERSION_DIR}/target/spec-to-aspect-1.0-SNAPSHOT-jar-with-dependencies.jar
TEMPLATE=${BASE_DIR}/proof-of-concept/src/main/java/edu/cornell/StateComparisonAspect.template
SETUP_FILE=${DATA_DIR}/initial-experiments/dsi-plus-experiment-subjects.txt
GENERATED_DATA_DIR=${SCRIPT_DIR}/data/generated-data
OUT_DIR=${GENERATED_DATA_DIR}/${PROJECT}
SMAPS_FILE=${DATA_DIR}/sMaps/${PROJECT}-master-spec-file.txt

date=`date +%Y-%m-%d-%H-%M-%S`
# backing up previous data if it exists
if [ -d ${OUT_DIR} ]; then
    echo "backing up old results..."
    mv ${GENERATED_DATA_DIR} ${GENERATED_DATA_DIR}-${date}
fi

ASPECTS_DIR=${OUT_DIR}/aspects
LOGS_DIR=${OUT_DIR}/logs

TIMINGS=${LOGS_DIR}/gol-timings
mkdir -p ${LOGS_DIR}
mkdir -p ${ASPECTS_DIR}

NAME=""
IN_FILE=${DATA_DIR}/initial-experiments/${PROJECT}-setup.csv
if [ "${PROJECT}" == commons-fileupload ]; then
    NAME=FileUpload
elif [ "${PROJECT}" == kamranzafar.jtar ]; then
    NAME=Jtar
else
    NAME=$( echo ${PROJECT} | cut -d- -f2 )
    NAME=${NAME^}
fi

echo "started script at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMINGS}
echo "started running specs in parallel at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMINGS}

(
    export NAME
    export PACKAGE
    export PROJECT
    export IN_FILE
    export ASPECTS_DIR
    export LOGS_DIR
    export SMAPS_FILE
    export SETUP_FILE
    cat ${IN_FILE} | grep -v spec-id | grep -v project | grep -v DYNAMIC_DISPATCH_SAME_METHOD | cut -d, -f1 | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/parallel-run-aspectj-set-get.sh
)

echo "ended running specs in parallel at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMINGS}
echo "ended parallel script at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMINGS}

# processing results

