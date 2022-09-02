SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
SUBJECTS_DIR=${SCRIPT_DIR}/subjects

bash ${SCRIPT_DIR}/run-all-dsi-plus-configs.sh ${SUBJECTS_DIR}/kamranzafar.jtar.txt -noMining
