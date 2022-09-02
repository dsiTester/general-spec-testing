if [ $# != 1 ]; then
    echo "Usage: $0 GENERATED_DATA_DIR"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
GENERATED_DATA_DIR=$1
OUT_DIR=${SCRIPT_DIR}/selection-stats

mkdir -p ${OUT_DIR}

function getTimeInSecs() {
    local time=$1
    hours=$( echo "${time}" | cut -d':' -f1 )
    mins=$( echo "${time}" | cut -d':' -f2 )
    secs=$( echo "${time}" | cut -d':' -f3 )
    echo "scale=2; (((${hours} * 60) + ${mins} ) * 60) + ${secs}" | bc -l
}

function compute_time() {
    local formatted=$1
    local path=$2
    
    start=$( grep "START TIME: " ${path}/ws/gol/dsi-results/all-tests/output-all-tests-${formatted}.log | rev | cut -d' ' -f1 | rev )
    end=$( grep "END TIME: " ${path}/ws/gol/dsi-results/all-tests/output-all-tests-${formatted}.log | rev | cut -d' ' -f1 | rev )
    startInSecs=$( getTimeInSecs "${start}" )
    endInSecs=$( getTimeInSecs "${end}" )
    diff=$( echo "${endInSecs} - ${startInSecs}" | bc )
    echo ${diff}
}

function main() {
    for proj in $( ls ${GENERATED_DATA_DIR} ); do
	[[ "${proj}" == "run-info" ]] && continue
	echo "computing stats for project ${proj}..."
    
	OUT_FILE=${OUT_DIR}/${proj}-stats.csv
	echo "spec-id,total-tc,selected-tc,time-tc,total-tm,selected-tm,time-tm,time-original" > ${OUT_FILE}

	num_specs=$( grep "# assignments:"  ${GENERATED_DATA_DIR}/${proj}/allTests_testClasses/logs/gol-all-tests | rev | cut -d' ' -f1 | rev )
	for i in $( seq 1 ${num_specs} ); do
	    formatted=$( printf "%05d" ${i} )
	    totalNumTestClass=$( grep "Total Number of Tests to Select from:" ${GENERATED_DATA_DIR}/${proj}/allTests_testClasses/logs/gol-all-tests | rev | cut -d' ' -f1 | rev )
	    testClass=$( grep -A1 "RUNNING SPEC ID #${i}" ${GENERATED_DATA_DIR}/${proj}/allTests_testClasses/logs/gol-all-tests | tail -n 1 | rev | cut -d' ' -f1 | rev )
	    if [[ "${testClass}" != ?(-)+([0-9]) ]]; then
		testClass=0
	    fi
	    testClassTime=$( compute_time ${formatted} ${GENERATED_DATA_DIR}/${proj}/allTests_testClasses )
	    totalNumTestMethod=$( grep "Total Number of Tests to Select from:" ${GENERATED_DATA_DIR}/${proj}/allTests_testMethods/logs/gol-all-tests | rev | cut -d' ' -f1 | rev )
	    testMethod=$( grep -A1 "RUNNING SPEC ID #${i}" ${GENERATED_DATA_DIR}/${proj}/allTests_testMethods/logs/gol-all-tests | tail -n 1 | rev | cut -d' ' -f1 | rev )
	    if [[ "${testMethod}" != ?(-)+([0-9]) ]]; then
	        testMethod=0
	    fi
	    testMethodTime=$( compute_time ${formatted} ${GENERATED_DATA_DIR}/${proj}/allTests_testMethods )
	    originalTime=$( compute_time ${formatted} ${GENERATED_DATA_DIR}/${proj}/allTests )
	    echo "${i},${totalNumTestClass},${testClass},${testClassTime},${totalNumTestMethod},${testMethod},${testMethodTime},${originalTime}" >> ${OUT_FILE}
	done
    done
}

main
