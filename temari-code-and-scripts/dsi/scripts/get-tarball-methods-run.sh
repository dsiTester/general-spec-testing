#!/bin/sh

# This script runs all granularities of DSI on the list of projects given.
if [ $# != 1 ]; then
    echo "Usage: $0 extracted_tarball"
    exit
fi

TARBALL_DIR=$1
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
OUT_DIR=${SCRIPT_DIR}/method-number-checks
OUT_FILE=${OUT_DIR}/summary-`date +%Y-%m-%d-%H-%M-%S`.csv
mkdir -p ${OUT_DIR}

function main() {
    (
	cd ${TARBALL_DIR}

	echo "Name,0,1,>1,Base Fail,Num Methods" > ${OUT_FILE}
	for config in $( ls | grep -v ".tgz" | grep "testMethods" | grep -v "NoDSI" ); do
	    name=$( echo "${config}" | cut -d'-' -f2- )
	    loc=${TARBALL_DIR}/${config}/${name}/testMethods/surefire-reports
	    echo "running get-project-methods-run.sh on ${name}..."
	    ( bash ${SCRIPT_DIR}/get-project-methods-run.sh ${loc} ${name} )
	    res=$( tail -n 1 ${OUT_DIR}/${name}-method-level.csv )
	    num=$( cat ${OUT_DIR}/${name}-method-level.csv | wc -l )
	    num=$((${num}-2))
	    res_stats=$( echo "${res}" | cut -d',' -f2- )
	    echo "${name},${res_stats},${num}" >> ${OUT_FILE}
	done
    )
}

main
