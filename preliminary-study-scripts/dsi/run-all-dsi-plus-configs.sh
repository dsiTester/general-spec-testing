PROJ_LIST=$1
MINING_OP=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

echo -e "\nRUNNING DSI WITH ALL GRANULARITIES WITH MINING OPTION ${MINING_OP}...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} dsiPlus-dsiAllGranularities -noBuild -backup ${MINING_OP}

if [ "${subject_file}" == *commons-exec* ]; then
    echo -e "\nRUNNING DSI WITH ALL GRANULARITIES WITH MINING OPTION ${MINING_OP}...\n"
    bash ${SCRIPT_DIR}/run-dsi-plus.sh ${PROJ_LIST} dsiPlus-dsiAllGranularities -noBuild -backup ${MINING_OP} ${SCRIPT_DIR}/exec_103_tests.txt
fi
