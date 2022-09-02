if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
NUM_THREADS=$1

bash ${SCRIPT_DIR}/mine-javert.sh kamranzafar.jtar ${NUM_THREADS}
bash ${SCRIPT_DIR}/process-javert-output.sh kamranzafar.jtar ${SCRIPT_DIR}/javert ${BASE_DIR}/util/spec-to-automata/
