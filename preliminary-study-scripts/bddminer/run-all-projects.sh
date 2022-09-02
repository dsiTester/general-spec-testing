if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
NUM_THREADS=$1

for project in $( cut -d' ' -f3 ${SCRIPT_DIR}/projects.txt ); do
    echo "Mining two-letter specs from project ${project}..."
    bash ${SCRIPT_DIR}/mine-bddminer.sh ${project} 2 ${NUM_THREADS}
    bash ${SCRIPT_DIR}/process-bddminer-output.sh ${project} ${SCRIPT_DIR}/bdd-miner-2 ${BASE_DIR}/util/spec-to-automata/ 96

    echo "Mining three-letter specs from project ${project}..."
    bash ${SCRIPT_DIR}/mine-bddminer.sh ${project} 3 ${NUM_THREADS}
    bash ${SCRIPT_DIR}/process-bddminer-output.sh ${project} ${SCRIPT_DIR}/bdd-miner-3 ${BASE_DIR}/util/spec-to-automata/ 96 3
done
