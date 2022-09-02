if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
NUM_THREADS=$1

bash ${SCRIPT_DIR}/run-dice-on-proj-in-parallel.sh kamranzafar.jtar ${NUM_THREADS}
