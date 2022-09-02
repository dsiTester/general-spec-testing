if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
NUM_THREADS=$1

for project in $( cat ${SCRIPT_DIR}/project-list.txt ); do
    echo "######RUNNING TEXADA ON PROJECT ${project}"
    bash ${SCRIPT_DIR}/run-texada-wrapper.sh ${project} ${NUM_THREADS}
    bash ${SCRIPT_DIR}/convert-texada-spec-to-fsm.sh ${project} ${NUM_THREADS}
done
