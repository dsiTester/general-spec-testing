if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
NUM_THREADS=$1

for project in $( cat ${SCRIPT_DIR}/projects.txt | cut -d' ' -f3 ); do
    bash ${SCRIPT_DIR}/state-experiment-wrapper.sh ${project} ${NUM_THREADS}
done
