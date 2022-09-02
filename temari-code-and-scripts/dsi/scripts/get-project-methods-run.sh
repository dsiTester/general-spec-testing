#!/bin/sh

# This script runs all granularities of DSI on the list of projects given.
if [ $# != 2 ]; then
    echo "Usage: $0 surefire_loc project_name"
    echo "where surefire_loc is the surefire-reports dir inside the project dir"
    exit
fi

LOC=$1
PROJ_NAME=$2
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
OUT_FILE=${SCRIPT_DIR}/method-number-checks/${PROJ_NAME}-method-level.csv
mkdir -p ${SCRIPT_DIR}/method-number-checks

function main() {
    echo "Method Name,0,1,>1,Base Fail" > ${OUT_FILE} 
    total_zero=0
    total_one=0
    total_multiple=0
    total_base_fail=0
    for f in $( ls ${LOC} ); do
	# name=$( echo "${f}" | cut -d'-' -f4 ) 
	zero="N"
	one="N"
	multiple="N"
        for res in $( grep -ir "Tests run: " ${LOC}/${f} | cut -d':' -f3 | cut -d' ' -f2 | cut -d',' -f1 | sort -u ); do
	    if [ "${res}" == 0 ]; then
		zero="Y"
		total_zero=$((${total_zero} + 1))
	    elif [ "${res}" == 1 ]; then
		one="Y"
	        total_one=$((${total_one} + 1))
	    elif [ "${res}" -gt 1 ]; then 
		multiple="Y"
		total_multiple=$((${total_multiple} + 1))
	    fi
	done
	baseline_fail_str=$( cat "${LOC}/../logs/gol-test-method-${f}" | grep "Baseline invocation fail" )
	if [ -z "${baseline_fail_str}" ]; then
	    base_fail="N"
	else
	    base_fail="Y"
	    total_base_fail=$((${total_base_fail} + 1 ))
	fi
	echo "${f},${zero},${one},${multiple},${base_fail}" >> ${OUT_FILE}
    done
    echo "TOTAL,${total_zero},${total_one},${total_multiple},${total_base_fail}"
    echo "TOTAL,${total_zero},${total_one},${total_multiple},${total_base_fail}" >> ${OUT_FILE}
}

main
