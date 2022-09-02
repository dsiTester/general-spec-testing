if [ $# -ne 1 ]; then
    echo "USAGE: $0 RUN_DIR"
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
OUT_DIR=${BASE_DIR}/data/inspections/sampled-inspections
mkdir -p ${OUT_DIR}

RUN_DIR=$1

function main() {
    for proj in $( ls ${RUN_DIR} ); do
        ( head -1 ${RUN_DIR}/${proj}/results/${proj}-results.csv ) > ${OUT_DIR}/${proj}.csv
    done


    for line in $( tail -n +2 ${SCRIPT_DIR}/out.txt); do
        project=$( echo "${line}" | cut -d, -f3 )
        spec_id=$( echo "${line}" | cut -d, -f2 )
        grep ${spec_id} ${RUN_DIR}/${project}/results/${project}-results.csv >> ${OUT_DIR}/${project}.csv
    done
}

main
