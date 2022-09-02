if [ $# -ne 3 ]; then
    echo "USAGE: bash $0 GENERATED_DATA_DIR AUTOMATON AUTOMATON_DIR"
    echo "ex AUTOMATON_DIR: ${SCRIPT_DIR}/automata"
    echo "NOTE: this script used to cycle through the entire GENERATED_DATA_DIR, but that causes a bug when automata are re-analyzed"
    echo "(because the AUTOMATON_DIR is now specific to the automaton, so there are no mapping files besides the most recent automata"
    exit
fi

GENERATED_DATA_DIR=$1
AUTOMATON_NAME=$2
AUTOMATON_DIR=$3

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname $( dirname ${SCRIPT_DIR} ) )
MANUAL_VERDICTS_DIR=${BASE_DIR}/data/dsi-spec-verdicts/

source ${BASE_DIR}/scripts/helper.sh

function get_intermediary_file_name() {
    local id=$1
    if [ ${id} -eq 1 ]; then
        echo a-always-followed-by-b
    elif [ ${id} -eq 2 ]; then
        echo a-never-followed-by-b
    elif [ ${id} -eq 3 ]; then
        echo a-always-preceded-by-b
    elif [ ${id} -eq 4 ]; then
        echo a-always-immediately-followed-by-b
    elif [ ${id} -eq 5 ]; then
        echo a-never-immediately-followed-by-b
    elif [ ${id} -eq 6 ]; then
        echo a-always-immediately-preceded-by-b
    else
        echo ${id}
    fi
}

function compare_with_ground_truth() {
    local spec="${1}"
    if grep -rq "${spec}" ${MANUAL_VERDICTS_DIR}; then
        verdict=$( grep -r "${spec}" ${MANUAL_VERDICTS_DIR} | rev | cut -d, -f1 | rev )
    else
        verdict="not-in-ground-truth"
    fi
    echo "${verdict}"
}

function compare_with_ground_truth_wrapper() {
    local a="$1"
    local b="$2"
    grep_a=$( echo "${a}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )
    grep_b=$( echo "${b}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )
    local constraint_name="${3}"
    if [[ ${constraint_name} == *-preceded-* ]]; then # if preceded, we should reverse a and b when looking for the ground truth (a should come after b)
        verdict=$( compare_with_ground_truth "${grep_b},${grep_a}" )
    else
        verdict=$( compare_with_ground_truth "${grep_a},${grep_b}" )
    fi
    echo "${a},${b},${verdict}"
}

function make_intermediary_file() {
    local id=$1
    local automaton_name=$2
    local results_dir=$3
    mapping_file=${AUTOMATON_DIR}/${automaton_name}.mapping
    out=${results_dir}/$( get_intermediary_file_name ${id} ).csv
    echo "method-a,method-b,verdict" > ${out}
    for constraint_name in $( grep "_${id}" ${holding_constraints_file} ); do
        a_alias=$( echo "${constraint_name}" | cut -d_ -f1 )
        b_alias=$( echo "${constraint_name}" | cut -d_ -f2 )
        if [ -f ${mapping_file} ]; then
            echo "mapping file ${mapping_file} exists!"
            a=$( grep ^${a_alias}, ${mapping_file} | cut -d, -f2 )
            b=$( grep ^${b_alias}, ${mapping_file} | cut -d, -f2 )
        else
            echo "mapping file ${mapping_file} does not exist!"
            a=${a_alias}
            b=${b_alias}
        fi
        ( compare_with_ground_truth_wrapper "${a}" "${b}" ${constraint_name} ) >> ${out}
    done
}

function create_automaton_summary() {
    local results_dir=$1
    local intermediate_results_dir=$2
    local out=${results_dir}/summary.csv
    echo "constraint-category,true-spec,sometimes-true-spec,spurious-spec,nbp,not-in-ground-truth,total-constraints" > ${out}
    for i in $( seq 1 6 | grep -v 2 | grep -v 5 ); do
        constraint_name=$( get_intermediary_file_name ${i} )
        constraint_file=${intermediate_results_dir}/${constraint_name}.csv
        true_spec=$( grep ",true-spec" ${constraint_file} | wc -l )
        sometimes_true_spec=$( grep ",sometimes-true-spec" ${constraint_file} | wc -l )
        spurious_spec=$( grep ",spurious-spec" ${constraint_file} | wc -l )
        nbp=$( grep ",no-break-pass" ${constraint_file} | wc -l )
        not_in_ground_truth=$( grep ",not-in-ground-truth" ${constraint_file} | wc -l )
        total=$( tail -n +2 ${constraint_file} | wc -l )
        echo "${constraint_name},${true_spec},${sometimes_true_spec},${spurious_spec},${nbp},${not_in_ground_truth},${total}" >> ${out}
    done
    compute_all_stats ${out}
}

function analyze() {
    local automaton=$1
    automaton_dir=${GENERATED_DATA_DIR}/${automaton}
    files_dir=${automaton_dir}/files
    results_dir=${automaton_dir}/results
    intermediate_results_dir=${automaton_dir}/per-constraint-formula-results
    holding_constraints_file=${results_dir}/constraints-that-hold.txt
    mkdir -p ${results_dir}
    mkdir -p ${intermediate_results_dir}

    grep ,HOLDS$ ${files_dir}/outcomes.txt > ${holding_constraints_file}
    for i in $( seq 1 6 | grep -v 2 | grep -v 5 ); do
        make_intermediary_file ${i} ${automaton} ${intermediate_results_dir}
    done
    create_automaton_summary ${results_dir} ${intermediate_results_dir}

    spurious_constraints_file=${results_dir}/spurious-constraints.csv
    echo "type,method-a,method-b" > ${spurious_constraints_file}
    for f in $( ls ${results_dir} | grep csv$ | grep -v summary | grep -v spurious-constraints.csv ); do
        type=$( echo "${f}" | cut -d. -f1 )
        grep "spurious-spec"$ ${results_dir}/${f} | cut -d, -f-2 | xargs -I {} echo "${type},{}" >> ${spurious_constraints_file}
    done
}

function main() {
    echo "===Analyzing automaton results: ${AUTOMATON_NAME}"
    analyze ${AUTOMATON_NAME}
}

main
