MINING_OP=$1                    # -noMining to skip mining

# This script runs DSI, DSI+, and all DSI+ optimizations on all of the subject projects.
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
SUBJECTS_DIR=${SCRIPT_DIR}/subjects

for subject_file in $( ls ${SUBJECTS_DIR} ); do
    bash ${SCRIPT_DIR}/run-all-dsi-plus-configs.sh ${SUBJECTS_DIR}/${subject_file} ${MINING_OP}
done
