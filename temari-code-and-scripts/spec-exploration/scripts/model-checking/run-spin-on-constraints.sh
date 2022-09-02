if [ $# -ne 4 ]; then
    echo "USAGE: bash $0 PML_FILE CONSTRAINT_FILE SPIN_DIR LOG_DIR"
    exit
fi

PML_FILE=$1
CONSTRAINT_FILE=$2
SPIN_DIR=$3
LOG_DIR=$4

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

function run_spin_wrapper() {
    cd ${SPIN_DIR}
    while read line
    do
        constraint=$(echo ${line} | cut -d' ' -f2)
        log_file=${LOG_DIR}/gol-${constraint}.txt
        sed -i '/ltl/d' ${PML_FILE}
        echo ${line} >> ${PML_FILE}
        ( time bash ${SCRIPT_DIR}/run-spin.sh ${PML_FILE} ${SPIN_DIR} ) &> ${log_file}
        cycles=$(grep "acceptance cycle" ${log_file})
        assert_fail=$(grep "assertion violated" ${log_file})
        syntax_error=$(grep "syntax error" ${log_file})
        if [[ -n "${cycles}" ]]; then
            echo ${constraint},CYCLE
        elif [[ -n "${assert_fail}" ]]; then
            echo ${constraint},ASSERT_FAIL
        elif [[ -n "${syntax_error}" ]]; then
            echo ${constraint},SYNTAX_ERROR
        else
            echo ${constraint},HOLDS
        fi
    done < ${CONSTRAINT_FILE}
}

run_spin_wrapper
