SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

MODEL_CHECKING_DIR=${SCRIPT_DIR}/spec-exploration/scripts/model-checking
NBP_CHECKER_DIR=${SCRIPT_DIR}/nbp-checker/nbp
STATE_CHECKER_DIR=${SCRIPT_DIR}/state-comparison/scripts
DSI_DIR=${SCRIPT_DIR}/dsi/scripts

DATA_DIR=${SCRIPT_DIR}/data/generated-data

function copy_dir_helper() {
    local from=$1
    local to=$2
    if [ -d ${to} ]; then
        return
    elif [ -d ${from} ]; then
        cp -r ${from} ${to}
    else
        mkdir ${to}
    fi
}

function copy() {
    for automaton in $( ls ${MODEL_CHECKING_DIR}/data/generated-data | grep -v new-sMaps | grep -v summary-results ); do
        mkdir -p ${DATA_DIR}/${automaton}
        copy_dir_helper ${MODEL_CHECKING_DIR}/data/generated-data/${automaton} ${DATA_DIR}/${automaton}/constraint-generation
        if [ -f ${MODEL_CHECKING_DIR}/data/generated-data/new-sMaps/${automaton}-sMap.txt ]; then
            cp ${MODEL_CHECKING_DIR}/data/generated-data/new-sMaps/${automaton}-sMap.txt ${DATA_DIR}/${automaton}/constraint-generation/results/
        fi
        copy_dir_helper ${NBP_CHECKER_DIR}/${automaton} ${DATA_DIR}/${automaton}/nbp-check
        copy_dir_helper ${STATE_CHECKER_DIR}/data/generated-data/${automaton} ${DATA_DIR}/${automaton}/state-comparison
        copy_dir_helper ${DSI_DIR}/data/generated-data/${automaton}/dsiPlus-dsiAllGranularities ${DATA_DIR}/${automaton}/dsi
    done
}

function setup() {
    # for automaton in $( ls ${MODEL_CHECKING_DIR}/data/generated-data | grep -v new-sMaps  | grep -v summary-results  ); do
    #     if [ -d ${DATA_DIR}/${automaton} ]; then
    #         mv ${DATA_DIR} ${DATA_DIR}-`date +%Y-%m-%d-%H-%M-%S`
    #         break
    #     fi
    # done
    mkdir -p ${DATA_DIR}
}

function get_dsi_res() {
    local id=$1
    local dsi_dir=$2
    if [ ! -f ${dsi_dir}/results/total-results/test-breakdown-all-tests.csv ]; then
        echo "${id},dsi-error"
        return
    fi
    line=$( grep ^"${id}", ${dsi_dir}/results/total-results/test-breakdown-all-tests.csv ) # might need to fix this later
    bucket=$( echo "${line}" | cut -d, -f4 )
    result=$( echo "${line}" | cut -d, -f5 )
    if [[ "${bucket}" == property-not-satisfied || "${bucket}" == error ]]; then
        echo "${id},unknown"
    else
        echo "${id},${result}" | sed 's/spurius/spurious/g'
    fi
}

function get_state_res() {
    local id=$1
    local state_file=$2
    local results_csv=$3
    if [ ! -f ${state_file} ]; then
	echo "${id},state-error" >> ${results_csv}
	echo "error"
    elif grep -q ^"${id},no" ${state_file} ; then
	echo "${id},does-not-share-state" >> ${results_csv}
	echo "does-not-share-state"
    else
	echo "shares-state"
    fi
}

function get_results_from_automaton() {
    local automaton=$1
    automaton_dir=${DATA_DIR}/${automaton}
    local_results_dir=${automaton_dir}/results
    mkdir -p ${local_results_dir}
    results_csv=${local_results_dir}/generalized-spec-testing-results.csv
    nbp_checker_dir=${DATA_DIR}/${automaton}/nbp-check
    state_comparison_dir=${DATA_DIR}/${automaton}/state-comparison
    dsi_dir=${DATA_DIR}/${automaton}/dsi
    echo "spec,result" > ${results_csv}  # maybe we want this to be more detailed later.
    if [ ! -f ${DATA_DIR}/${automaton}/constraint-generation/results/${automaton}-sMap.txt ]; then
        echo "No sMap from automaton ${automaton}!!"
        return
    fi
    for id in $( cat ${DATA_DIR}/${automaton}/constraint-generation/results/${automaton}-sMap.txt | cut -d' ' -f1 ); do
        if grep -q ^"${id} " ${nbp_checker_dir}/results/nbp-specs.txt ; then # nbp has been spotted
            echo "${id},NBP" >> ${results_csv}
        elif [[ $( get_state_res ${id} ${state_comparison_dir}/results/${automaton}-results.csv ${results_csv} ) == "shares-state" ]] ; then # gotten to DSI
            get_dsi_res ${id} ${dsi_dir} >> ${results_csv}
        fi
    done
}

function summary() {
    results=${DATA_DIR}/summary-results.csv
    mkdir -p ${results_dir}
    echo "automaton,likely-valid-constraints,likely-spurious-constraints,unknown-constraints,NBP-constraints,total" > ${results}
    for automaton in $( ls ${DATA_DIR} | grep -v summary-results ); do
        automaton_results=${DATA_DIR}/${automaton}/results/generalized-spec-testing-results.csv
        total=$( cat ${automaton_results} | wc -l | xargs -I {} echo "{} - 1" | bc -l )
        nbp=$( grep ",NBP" ${automaton_results} | wc -l )
        not_share_state=$( grep ",does-not-share-state" ${automaton_results} | wc -l )
        spurious=$( grep ",spurious-spec" ${automaton_results} | wc -l )
        spurious=$( echo "${spurious} + ${not_share_state}" | bc -l )
        unknown=$( grep "unknown" ${automaton_results} | wc -l )
        valid=$( grep "true" ${automaton_results} | wc -l )
        echo "${automaton},${valid},${spurious},${unknown},${nbp},${total}" >> ${results}
    done
}

function main() {
    setup
    copy
    for automaton in $( ls ${DATA_DIR} ); do
        get_results_from_automaton ${automaton}
    done
    summary
}

main
