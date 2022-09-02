#!/bin/sh

if [ $# != 1 ]; then
    echo "usage: bash $0 CONFIG"
    exit
fi

CONFIG=$1

id=$( echo "${CONFIG}" | cut -d'@' -f1 )
smap=$( echo "${CONFIG}" | cut -d'@' -f2 )
optimization_op_num=$( echo "${CONFIG}" | cut -d'@' -f3 )

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
project_location=`dirname ${PWD}`
comp_log_file=${project_location}/logs/gol-compile-${id}
log_file=${project_location}/logs/gol-spec-based-${id}
master_spec_file=${project_location}/logs/master-spec-file.txt
total_spec_num=$( cat ${master_spec_file} | wc -l )

# setting failEarly and selection options
if [ ${optimization_op_num} -eq 2 ]; then
    optimization_op="-DfailEarlyOp=fail-on-not-true -DselectionOp=no-selection"
elif [ ${optimization_op_num} -eq 3 ]; then
    optimization_op="-DfailEarlyOp=fail-on-not-true -DselectionOp=selection"
elif [ ${optimization_op_num} -eq 4 ]; then
    optimization_op="-DfailEarlyOp=run-all -DselectionOp=selection"
elif [ ${optimization_op_num} -eq 5 ]; then
    optimization_op="-DfailEarlyOp=fail-on-spurious -DselectionOp=no-selection"
elif [ ${optimization_op_num} -eq 6 ]; then
    optimization_op="-DfailEarlyOp=fail-on-spurious -DselectionOp=selection"
else # if we don't get either 2/3/4/5/6 optimization, we will run the default (no optimizations)
    optimization_op="-DfailEarlyOp=run-all -DselectionOp=no-selection"
fi

echo "RUNNING DSI+ ON SPEC ID ${id} out of total ${total_spec_num} specs with optimization options ${optimization_op}"

# uncomment to test compile for the specific build dir
# ( time mvn clean test-compile ${COMP_SKIPS} -DbuildDirectory=${id} -DtempDir=${id} ) >> ${comp_log_file} 2>&1

# copying over the originally compiled target directory to avoid extra work...
cp -r target ${id}

( time mvn dsi:run-spec-based-dsi ${SKIPS} -DskipTraceCollection=true -Did=${id} -DspecToTestsMap=${smap} -DbuildDirectory=${id} -DtempDir=${id} ${optimization_op} ) >> ${log_file} 2>&1

# delete to save space
rm -rf ${id}
