SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )

line=$1

cd ${SPIN_DIR}

constraint=$(echo ${line} | cut -d' ' -f2)
CONSTRAINT_PML_FILE=${FILES_DIR}/${constraint}.pml
cp ${PML_FILE} ${CONSTRAINT_PML_FILE}

log_file=${LOGS_DIR}/gol-${constraint}.txt
# sed -i '/ltl/d' ${CONSTRAINT_PML_FILE}
echo ${line} >> ${CONSTRAINT_PML_FILE}
( time bash ${SCRIPT_DIR}/run-spin-for-parallel.sh ${CONSTRAINT_PML_FILE} ${SPIN_DIR} ${constraint} ) &> ${log_file}
cycles=$(grep "acceptance cycle" ${log_file})
assert_fail=$(grep "assertion violated" ${log_file})
syntax_error=$(grep "syntax error" ${log_file})
no_errors=$( grep "errors: 0" ${log_file})
if [[ -n "${cycles}" ]]; then
    echo ${constraint},CYCLE
elif [[ -n "${assert_fail}" ]]; then
    echo ${constraint},ASSERT_FAIL
elif [[ -n "${syntax_error}" ]]; then
    echo ${constraint},SYNTAX_ERROR
elif [[ -n "${no_errors}" ]]; then
    echo ${constraint},HOLDS
else
    echo ${constraint},ERROR
fi
