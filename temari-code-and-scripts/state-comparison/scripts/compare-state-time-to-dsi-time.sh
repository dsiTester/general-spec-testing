if [ $# -ne 3 ]; then
    echo "USAGE: $0 PROJECT STATE_OUT SCRIPT_OUT"
    echo "where STATE_OUT is the project experimentation directory (ex. data/generated-data/kamranzafar.jtar )"
    echo "where SCRIPT_OUT is where the results of this script will be written to"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data

PROJECT=$1
STATE_DIR=$2
SCRIPT_OUT=$3
mkdir -p ${SCRIPT_OUT}

STATE_RESULTS_FILE=${STATE_DIR}/results/${PROJECT}-results.csv
if [ ! -f ${STATE_RESULTS_FILE} ]; then
    echo "${STATE_RESULTS_FILE} does not exist!"
    echo "NOTE: process-aspectj-set-gets.sh should be run before this script"
    exit
fi
STATE_OUT=${STATE_DIR}/logs

SETUP_FILE=${DATA_DIR}/initial-experiments/${PROJECT}-setup.csv
DSI_TIMES_FILE=${DATA_DIR}/dsi-times-csvs/${PROJECT}-dsi-times.csv
OUT=${SCRIPT_OUT}/${PROJECT}-timings.csv

source ${SCRIPT_DIR}/helper.sh

function get_state_time_in_sec() {
    local state_file=$1
    state_time_min=$( grep ^real ${state_file} | cut -f2 | cut -d'm' -f1 )
    state_time_sec=$( grep ^real ${state_file} | cut -f2 | cut -d'm' -f2 | cut -d's' -f1 )
    echo "(${state_time_min} * 60 ) + ${state_time_sec}" | bc -l
}

function get_jdk_specific_time_in_sec() {
    local id=$1
    local identifier=$2
    jdk_file=${STATE_OUT}/gol-${identifier}-${id}
    if [ -f ${jdk_file} ]; then
        get_state_time_in_sec ${jdk_file}
    else
        echo 0
    fi
}

function main() {
    acc=0 # number of ids checked, so we can compute an average
    echo "id,state-compile-time,state-run-time,jdk-weave-time,jdk-run-compile,jdk-run-time,total-state-time(wo/JDK),total-state-time(w/JDK),total-state-time(w/JDK+DSI),only-dsi-time(seq)" > ${OUT}
    for id in $( tail -n +2 ${STATE_RESULTS_FILE} | cut -d, -f1 ); do # cat ${SETUP_FILE}  | grep ^0
        state_compile_file=${STATE_OUT}/gol-test-compile-${id}
        state_out_file=${STATE_OUT}/gol-state-check-${id}
        if ! grep -q "before calling method-a" ${state_out_file}; then
	    continue
        elif ! grep -q "before calling method-b" ${state_out_file}; then
	    continue
        fi
        state_compile_time=$( get_state_time_in_sec ${state_compile_file} )
        state_test_time=$( get_state_time_in_sec ${state_out_file} )
        jdk_weave_time=$( get_jdk_specific_time_in_sec ${id} weave-jdk )
        jdk_compile_time=$( get_jdk_specific_time_in_sec ${id} jdk-run-test-compile )
        jdk_test_time=$( get_jdk_specific_time_in_sec ${id} jdk-run )
        total_state_time_wo_jdk=$( echo "${state_compile_time} + ${state_test_time}" | bc -l )
        total_state_time_w_jdk=$( echo "${state_compile_time} + ${state_test_time} + ${jdk_weave_time} + ${jdk_compile_time} + ${jdk_test_time}" | bc -l )
        dsi_time=$( grep ^"${id}", ${DSI_TIMES_FILE} | cut -d, -f2 )
        state_verdict=$( grep ^"${id}", ${STATE_RESULTS_FILE} | cut -d, -f2 )
        if [ "${state_verdict}" == "no" ]; then
            total_state_time_w_jdk_dsi="${total_state_time_w_jdk}"
        else # either yes or err
            total_state_time_w_jdk_dsi=$( echo "${total_state_time_w_jdk} + ${dsi_time}" | bc -l )
        fi

        echo "${id},${state_compile_time},${state_test_time},${jdk_weave_time},${jdk_compile_time},${jdk_test_time},${total_state_time_wo_jdk},${total_state_time_w_jdk},${total_state_time_w_jdk_dsi},${dsi_time}"
        acc=$((acc + 1))
    done
}

main >> ${OUT}

compute_totals ${OUT}
compute_avg ${OUT}
