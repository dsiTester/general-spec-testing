# this script generates new sMaps for the not-yet-mined specs
if [ $# -lt 2 ]; then
    echo "USAGE: bash $0 GENERATED_DATA_DIR SPEC_OPTION [AUTOMATON_NAME]"
    echo "if SPEC_OPTION is -ALL then the script will include all 2-letter \"followed\" constraints"
    echo "otherwise, it will only include those that are not in the ground truth set"
    echo "if AUTOMATON_NAME is not blank, then the script will only create inputs for a single automaton"
    exit
fi

GENERATED_DATA_DIR=$1
SPEC_OPTION=$2
AUTOMATON=$3

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
OUT_DIR=${GENERATED_DATA_DIR}/new-sMaps # will have a subdirectory that is only for sMaps
if [ "${AUTOMATON}" == "" ]; then
    rm -rf ${OUT_DIR}
fi
mkdir -p ${OUT_DIR}

function get_constraint_pairs() {
    local automata=$1
    automata_dir=${GENERATED_DATA_DIR}/${automata}
    intermediate_results_dir=${automata_dir}/per-constraint-formula-results
    for f in $( ls ${intermediate_results_dir} ); do
        [[ ${f} == *never* ]] && continue # we don't know how to deal with "never"s yet, so we will skip them
        if [[ "${f}" == *followed* ]]; then
            # will deal with precedes later
            typ=$( echo "${f}" | cut -d. -f1 )
            if [ ${SPEC_OPTION} != "-ALL" ]; then
                grep "not-in-ground-truth" ${intermediate_results_dir}/${f} | cut -d, -f-2 | sort -u | xargs -I {} echo "{},${typ}" >> ${OUT_DIR}/tmp-${automata}.txt
            else
                tail -n +2 ${intermediate_results_dir}/${f} | cut -d, -f-2 | sort -u | xargs -I {} echo "{},${typ}" >> ${OUT_DIR}/tmp-${automata}.txt
            fi
        elif [[ "${f}" == *preceded* ]]; then
            typ=$( echo "${f}" | cut -d. -f1 )
            if [ "${SPEC_OPTION}" != "-ALL" ]; then
                for pair in $( grep "not-in-ground-truth" ${intermediate_results_dir}/${f} | cut -d, -f-2 | sort -u ); do
                    a=$( echo "${pair}" | cut -d, -f1 )
                    b=$( echo "${pair}" | cut -d, -f2 )
                    echo "${b},${a},${typ}" >> ${OUT_DIR}/tmp-${automata}.txt
                done
            else
                for pair in $( tail -n +2 ${intermediate_results_dir}/${f} | cut -d, -f-2 | sort -u ); do
                    a=$( echo "${pair}" | cut -d, -f1 )
                    b=$( echo "${pair}" | cut -d, -f2 )
                    echo "${b},${a},${typ}" >> ${OUT_DIR}/tmp-${automata}.txt
                done
            fi
        fi
    done
    acc=1
    for spec in $( cat ${OUT_DIR}/tmp-${automata}.txt | cut -d, -f-2 | sort -u ); do
        a=$( echo "${spec}" | cut -d, -f1 )
        b=$( echo "${spec}" | cut -d, -f2 )
        id=$( printf "%05d" ${acc} )
        grep_spec=$( echo "${spec}" | sed 's/\[/\\[/g' | sed 's/\$/\\$/g' )
        constraint_types=$( grep "${grep_spec}" ${OUT_DIR}/tmp-${automata}.txt | cut -d, -f3 | paste -sd';' )
        echo "${id} ${a} ${b} all-tests ${constraint_types}" >> ${OUT_DIR}/${automata}-sMap.txt
        acc=$(( acc + 1 ))
    done
    cp ${OUT_DIR}/${automata}-sMap.txt ${GENERATED_DATA_DIR}/${automata}/results/generated-sMap.txt
}

function main() {
    if [ "${AUTOMATON}" != "" ]; then
        echo =====Getting constraints pairs from ${AUTOMATON}
        ( get_constraint_pairs ${AUTOMATON} )
    else
        for automata in $( ls ${GENERATED_DATA_DIR} | grep -v new-sMap ); do
            miner=$( echo "${automata}" | cut -d@ -f1 )
            project=$( echo "${automata}" | cut -d@ -f2 )
            complex_spec_id=$( echo "${automata}" | cut -d@ -f3 )
            echo =====Getting constraints pairs from ${automata}
            ( get_constraint_pairs ${automata} )
        done
    fi
}

main
