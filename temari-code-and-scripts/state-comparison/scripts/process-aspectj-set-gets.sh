if [ $# -ne 3 ]; then
    echo "USAGE: $0 PROJECT_NAME IN_DIR SCRIPT_OUT"
    echo "where SCRIPT_OUT is where the results of this script will be written to"
    exit
fi

PROJECT=$1
IN_DIR=$2
OUT_DIR=$3
mkdir -p ${OUT_DIR}

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
SETUP_FILE=${DATA_DIR}/initial-experiments/${PROJECT}-setup.csv

OUT_FILE=${OUT_DIR}/${PROJECT}-results.csv

source ${SCRIPT_DIR}/state_helper.sh

function get_updated_dsi_verdict() {
    local state_verdict=$1
    local original=$2
    if [ "${state_verdict}" == "no" ]; then
        echo "ls"
    else
        echo "${original}"
    fi
}

function main() {

    echo "id,share-state(tool),original-dsi,updated-dsi,verdict,manual-tags,a-set-b-set,a-set-b-get,a-get-b-set,a-get-b-get,used-jdk?" > ${OUT_FILE}

    for spec_id in $( ls ${IN_DIR} | grep "gol-state-check" | cut -d- -f4 ); do
        if [ -f ${IN_DIR}/gol-jdk-run-${spec_id} ]; then
            # if there was an additional JDK run, then we should check that
            # (if the two methods shared state in the initial run, we won't be doing the JDK run)
            in_file=gol-jdk-run-${spec_id}
            jdk_use="yes"
        else
            in_file=gol-state-check-${spec_id}
            jdk_use="no"
        fi
        dsi_verdict=$( grep "^${spec_id}" ${SETUP_FILE} | cut -d, -f5 )
        manual_verdict=$( grep "^${spec_id}" ${SETUP_FILE} | cut -d, -f6 )
        tags=$( grep "^${spec_id}" ${SETUP_FILE} | cut -d, -f7 )
        if [[ "${manual_verdict_and_tags}" == *"DYNAMIC_DISPATCH_SAME_METHOD"* ]]; then
            echo "skipping ${spec_id} due to DYNAMIC_DISPATCH_SAME_METHOD"
            continue
        fi

        if ! grep -q "before calling method-a" ${IN_DIR}/${in_file}; then
            echo "${spec_id},err,${dsi_verdict},${dsi_verdict},${manual_verdict},${tags},-,-,-,-,${jdk_use}" >> ${OUT_FILE}
	    continue
        elif ! grep -q "before calling method-b" ${IN_DIR}/${in_file}; then
            echo "${spec_id},err,${dsi_verdict},${dsi_verdict},${manual_verdict},${tags},-,-,-,-,${jdk_use}" >> ${OUT_FILE}
	    continue
        fi

        res=$( compute_share_state ${IN_DIR}/${in_file} )
        share_state=$( echo "${res}" | cut -d, -f1 )
        num_share_breakdown=$( echo "${res}" | cut -d, -f2- )
        updated_dsi_verdict=$( get_updated_dsi_verdict "${share_state}" "${dsi_verdict}" )
        echo "${spec_id},${share_state},${dsi_verdict},${updated_dsi_verdict},${manual_verdict},${tags},${num_share_breakdown},${jdk_use}" >> ${OUT_FILE}
    done

}

main
