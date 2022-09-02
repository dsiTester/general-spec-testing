if [ $# -lt 3 ]; then
    echo "USAGE: $0 IDENT PROJECT SMAP NUM_THREADS"
    exit
fi

IDENT=$1
PROJECT=$2
SMAPS_FILE=$3
NUM_THREADS=$4

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
CONVERSION_DIR=${BASE_DIR}/spec-to-aspect
CONVERSION_JAR=${CONVERSION_DIR}/target/spec-to-aspect-1.0-SNAPSHOT-jar-with-dependencies.jar
TEMPLATE=${BASE_DIR}/proof-of-concept/src/main/java/edu/cornell/StateComparisonAspect.template
SETUP_FILE=${DATA_DIR}/initial-experiments/dsi-plus-experiment-subjects.txt
GENERATED_DATA_DIR=${SCRIPT_DIR}/data/generated-data
OUT_DIR=${GENERATED_DATA_DIR}/${IDENT}

source ${SCRIPT_DIR}/state_helper.sh

date=`date +%Y-%m-%d-%H-%M-%S`
# backing up previous data if it exists
if [ -d ${OUT_DIR} ]; then
    echo "backing up old results..."
    mv ${GENERATED_DATA_DIR} ${GENERATED_DATA_DIR}-${date}
fi

ASPECTS_DIR=${OUT_DIR}/aspects
LOGS_DIR=${OUT_DIR}/logs
RESULTS_DIR=${OUT_DIR}/results
OUT_FILE=${RESULTS_DIR}/${IDENT}-results.csv

TIMINGS=${LOGS_DIR}/gol-timings
mkdir -p ${LOGS_DIR}
mkdir -p ${ASPECTS_DIR}
mkdir -p ${RESULTS_DIR}

NAME=""
if [ "${PROJECT}" == commons-fileupload ]; then
    NAME=FileUpload
elif [ "${PROJECT}" == kamranzafar.jtar ]; then
    NAME=Jtar
else
    NAME=$( echo ${PROJECT} | cut -d- -f2 )
    NAME=${NAME^}
fi

echo "started running specs in sequence at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMINGS}

for id in $( cat ${SMAPS_FILE} | cut -d' ' -f1 ); do
    (
        export NAME
        export PACKAGE
        export PROJECT
        export ASPECTS_DIR
        export LOGS_DIR
        export SETUP_FILE
        export SMAPS_FILE
        time bash ${SCRIPT_DIR}/parallel-run-aspectj-set-get.sh ${id}
    ) &> ${LOGS_DIR}/gol-seq-run-state-checker-${id}
done

echo "ended running specs in sequence at: `date +%Y-%m-%d-%H-%M-%S`" | tee -a ${TIMINGS}

echo "Processing state-checked specs..."

echo "spec-id,share-state?,ran-jdk?" > ${OUT_FILE}
SMAP_FOR_DSI=${RESULTS_DIR}/${IDENT}-remaining-specs-sMap.txt
>${SMAP_FOR_DSI}
for spec_id in $( ls ${LOGS_DIR} | grep "gol-state-check" | cut -d- -f4- | sort -u ); do
    if [ -f ${LOGS_DIR}/gol-jdk-run-${spec_id} ]; then
        # if there was an additional JDK run, then we should check that
        # (if the two methods shared state in the initial run, we won't be doing the JDK run)
        in_file=${LOGS_DIR}/gol-jdk-run-${spec_id}
        jdk_use="yes"
    else
        in_file=${LOGS_DIR}/gol-state-check-${spec_id}
        jdk_use="no"
    fi

    if ! grep -q "before calling method-a" ${in_file}; then
        share_state=err
    elif ! grep -q "before calling method-b" ${in_file}; then
        share_state=err
    else
        res=$( compute_share_state ${in_file} )
        share_state=$( echo "${res}" | cut -d, -f1 )
    fi

    echo ${spec_id},${share_state},${jdk_use} >> ${OUT_FILE}

    if [[ "${share_state}" != no ]]; then # output
        grep "${spec_id}" ${SMAPS_FILE} >> ${SMAP_FOR_DSI}
    fi

done
