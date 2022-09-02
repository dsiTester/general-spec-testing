if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
NUM_THREADS=$1

echo "Mining two-letter specs..."
bash ${SCRIPT_DIR}/mine-bddminer.sh kamranzafar.jtar 2 ${NUM_THREADS}
bash ${SCRIPT_DIR}/process-bddminer-output.sh kamranzafar.jtar ${SCRIPT_DIR}/bdd-miner-2 ${BASE_DIR}/util/spec-to-automata/ 96

echo "Mining three-letter specs..."
bash ${SCRIPT_DIR}/mine-bddminer.sh kamranzafar.jtar 3 ${NUM_THREADS}
bash ${SCRIPT_DIR}/process-bddminer-output.sh kamranzafar.jtar ${SCRIPT_DIR}/bdd-miner-3 ${BASE_DIR}/util/spec-to-automata/ 96 3
