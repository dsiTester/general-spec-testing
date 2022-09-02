if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
NUM_THREADS=$1

for project in $( cut -d' ' -f3 ${SCRIPT_DIR}/projects.txt ); do
    echo "Running Javert on ${project}..."
    bash ${SCRIPT_DIR}/mine-javert.sh ${project} ${NUM_THREADS}
    bash ${SCRIPT_DIR}/process-javert-output.sh ${project} ${SCRIPT_DIR}/javert ${BASE_DIR}/util/spec-to-automata/
done
