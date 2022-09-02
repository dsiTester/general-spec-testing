if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

NUM_THREADS=$1
SCRIPT_DIR=$( cd ${ dirname $0 ) && pwd )

for project in $( cat ${SCRIPT_DIR}/projects.txt ); do
    bash ${SCRIPT_DIR}/run-dsm-manual.sh ${project} ${NUM_THREADS}
    bash ${SCRIPT_DIR}/run-dsm-randoop.sh ${project} ${NUM_THREADS}
done
