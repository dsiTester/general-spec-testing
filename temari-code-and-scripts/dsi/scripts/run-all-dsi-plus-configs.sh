PROJ_LIST=$1
BUILD_OP=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

if [[ ${BUILD_OP} == "build" ]]; then
    bash ${SCRIPT_DIR}/install_dsi_plugin.sh
fi

echo -e "\nRUNNING DSI WITH ALL GRANULARITIES WITH MINING OPTION ${MINING_OP}...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} dsiPlus-dsiAllGranularities -noBuild -noBackup -noMining
echo -e "\nRUNNING DSI+ WITH ALL GRANULARITIES...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} dsiPlus-allGranularities -noBuild -noBackup -noMining
# echo -e "\nRUNNING DSI+ WITH ALL TESTS GRANULARITY...\n"
# bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} dsiPlus-allTests -noBuild -noBackup
# echo -e "\nRUNNING DSI+ WITH TEST CLASSES GRANULARITY...\n"
# bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} dsiPlus-testClasses -noBuild -noBackup
# echo -e "\nRUNNING DSI+ WITH TEST METHODS GRANULARITY...\n"
# bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} dsiPlus-testMethods -noBuild -noBackup



echo -e "\nRUNNING DSI++ WITH LEVEL 1 OPTIMIZATION...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} optimizedDsiPlus-1 -noBuild -noBackup -noMining
echo -e "\nRUNNING DSI++ WITH LEVEL 2 OPTIMIZATION...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} optimizedDsiPlus-2 -noBuild -noBackup -noMining
echo -e "\nRUNNING DSI++ WITH LEVEL 3 OPTIMIZATION...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} optimizedDsiPlus-3 -noBuild -noBackup -noMining
echo -e "\nRUNNING DSI++ WITH LEVEL 4 OPTIMIZATION...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} optimizedDsiPlus-4 -noBuild -noBackup -noMining
echo -e "\nRUNNING DSI++ WITH LEVEL 5 OPTIMIZATION...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} optimizedDsiPlus-5 -noBuild -noBackup -noMining
echo -e "\nRUNNING DSI++ WITH LEVEL 6 OPTIMIZATION...\n"
bash ${SCRIPT_DIR}/run-dsi-plus-experiments.sh ${PROJ_LIST} optimizedDsiPlus-6 -noBuild -noBackup -noMining
