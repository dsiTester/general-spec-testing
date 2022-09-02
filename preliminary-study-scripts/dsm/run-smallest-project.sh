if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 NUM_THREADS"
    exit
fi

NUM_THREADS=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

bash ${SCRIPT_DIR}/run-dsm-manual.sh kamranzafar.jtar ${NUM_THREADS}
bash ${SCRIPT_DIR}/run-dsm-randoop.sh kamranzafar.jtar ${NUM_THREADS}
