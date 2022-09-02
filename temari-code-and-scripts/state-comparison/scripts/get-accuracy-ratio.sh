if [[ $# -ne 2 ]]; then
    echo "USAGE: $0 IN OLD_IN"
    exit
fi

IN=$1
OLD_IN=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
SETUP_FILE=${DATA_DIR}/initial-experiments/${PROJECT}-setup.csv

source ${SCRIPT_DIR}/helper.sh

function accuracy() {
    local line=$1
    local updated_verdict=$( echo "${line}" | cut -d, -f4 )
    local manual_verdict=$( echo "${line}" | cut -d, -f5 )
    if [[ "${updated_verdict}" == ls && "${manual_verdict}" == "spurious-spec" ]]; then
        echo "yes"
    elif [[ "${updated_verdict}" == lv && "${manual_verdict}" == *"true-spec" ]]; then
        echo "yes"
    else
        echo "no"
    fi
}

function get_treat_special_affected_accuracy() {

    for proj in $( ls ${IN} ); do
        echo =====${proj}
        accurate_acc=0
        total_acc=0
        for spec_id in $( comm -12 <( find ${IN}/${proj} -name gol-treat-special-* | rev | cut -d- -f1 | rev | sort -u ) <( grep ",yes"$ ${IN}/${proj}/results/${proj}-results.csv | cut -d, -f1 | sort -u ) ); do
            line=$( grep ^"${spec_id}" ~/projects/state-comparison/scripts/generated-data-jdk-first-pass/${proj}/results/${proj}-results.csv )
            accuracy=$( accuracy ${line} )
            if [ "${accuracy}" == "yes" ]; then
                accurate_acc=$(( accurate_acc + 1 ))
            else
                echo "${spec_id}: "$( cat ${IN}/${proj}/logs/gol-treat-special-${spec_id})
            fi
            total_acc=$(( total_acc + 1 ))
        done
        if [[ "${total_acc}" -ne 0 ]]; then
            percent_accurate=$( echo "scale=4; (${accurate_acc} / ${total_acc}) *100 " | bc -l )
            echo "# accurate: ${accurate_acc}; # total: ${total_acc}; % accuracy: ${percent_accurate}"
        fi
    done

}

function get_changed_verdict_accuracy() {
    local out=${DATA_DIR}/initial-experiments/jdk-accuracy-change.csv
    echo "project,#improved,#worsened,#accuracy-changed,#accuracy-not-changed,#total-accurate(JDK),total" > ${out}
    for proj in $( ls ${IN} ); do
        results_file=${IN}/${proj}/results/${proj}-results.csv
        old_results_file=${OLD_IN}/${proj}/results/${proj}-results.csv
        accurate_acc=0
        total_acc=0
        improved_accuracy_acc=0
        worsened_accuracy_acc=0
        num_no_change_in_overall_accuracy=0
        for diff_line in $( diff <( grep ",yes"$ ${results_file} | rev | cut -d, -f2- | rev ) ${old_results_file} | grep ^"<" | cut -d' ' -f2- ); do
            id=$( echo "${diff_line}" | cut -d, -f1 )
            accuracy=$( accuracy ${diff_line} )
            old_accuracy=$( accuracy $( grep "${id}" ${old_results_file} ) )
            if [[ "${accuracy}" == "${old_accuracy}" ]]; then
                num_no_change_in_overall_accuracy=$(( num_no_change_in_overall_accuracy + 1 ))
            fi
            if [[ "${accuracy}" != "${old_accuracy}" && "${accuracy}" == yes ]]; then
                improved_accuracy_acc=$(( improved_accuracy_acc + 1 ))
            elif [[ "${accuracy}" != "${old_accuracy}" && "${accuracy}" == no ]]; then
                worsened_accuracy_acc=$(( worsened_accuracy_acc + 1 ))
            fi
            if [ "${accuracy}" == "yes" ]; then
                accurate_acc=$(( accurate_acc + 1 ))
            fi
            total_acc=$(( total_acc + 1 ))
        done
        if [[ "${total_acc}" -ne 0 ]]; then
            percent_accurate=$( echo "scale=4; (${accurate_acc} / ${total_acc}) *100 " | bc -l )
        else
            percent_accurate=0
        fi
        # echo "# accurate: ${accurate_acc}; # total: ${total_acc}; % accuracy: ${percent_accurate}"
        # echo "# changed: $(( ${improved_accuracy_acc} + ${worsened_accuracy_acc} )); # improved: ${improved_accuracy_acc}; # worsened: ${worsened_accuracy_acc}"
        # echo "# no change in overall accuracy: ${num_no_change_in_overall_accuracy}"
        echo "${proj},${improved_accuracy_acc},${worsened_accuracy_acc},$(( ${improved_accuracy_acc} + ${worsened_accuracy_acc} )),${num_no_change_in_overall_accuracy},${accurate_acc},${total_acc}" >> ${out}
    done
    compute_totals ${out}
    compute_avg ${out}
}

get_treat_special_affected_accuracy
get_changed_verdict_accuracy
