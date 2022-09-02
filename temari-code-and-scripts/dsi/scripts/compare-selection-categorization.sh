if [ $# != 1 ]; then
    echo "Usage: $0 GENERATED_DATA_DIR"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
GENERATED_DATA_DIR=$1

TIMESTAMP=`date +%Y-%m-%d-%H-%M-%S`
OUT_DIR=${SCRIPT_DIR}/selection-categorization-comparison
OUT_FILE=${OUT_DIR}/comparison-${TIMESTAMP}.csv
NOT_MATCH_FILE=${OUT_DIR}/not-match-${TIMESTAMP}.csv

mkdir -p ${OUT_DIR}

function compare() {
    local proj=$1
    local config=$2

    match=0
    not_match=0
    
    diff=$( diff ${GENERATED_DATA_DIR}/${proj}/allTests/surefire-reports/all-tests/spec-order.txt ${GENERATED_DATA_DIR}/${proj}/${config}/surefire-reports/all-tests/spec-order.txt )
    num_specs=$( cat ${GENERATED_DATA_DIR}/${proj}/allTests/surefire-reports/all-tests/spec-order.txt | wc -l )
    cd ${GENERATED_DATA_DIR}/${proj}
    if [ "${diff}" == "" ]; then
	echo "SPEC_ORDER files agree..."
	
	for i in $( seq 1 ${num_specs} ); do
	    formatted=$( printf "%05d" ${i} )
	    uniq=$( find -name output-all-tests-${formatted}.log | grep -v "ws" | grep -e "${config}/" -e "allTests/" | rev | cut -d'/' -f2 | rev | uniq | wc -l )
	    if [ ${uniq} -eq 1 ]; then
		match=$((${match} + 1))
	    else
		not_match=$((${not_match} + 1))
		echo "output-all-tests-${formatted}.log" >> ${NOT_MATCH_FILE}
	    fi 
	done

	echo "${proj}-${config},${match},${not_match}" >> ${OUT_FILE}
    else 
	echo "SPEC_ORDER file does not agree..." | tee -a ${OUT_FILE}
	echo "${proj}-${config},-,-" >> ${OUT_FILE}
	# need more complicated routine here
	for i in $( seq 1 ${num_specs} ); do
	    formatted=$( printf "%05d" ${i} )
	    spec=$( cat allTests/ws/gol/dsi-results/all-tests/output-all-tests-${formatted}.log | grep -e "a=" -e "b=" )
	    spec=$( echo ${spec} | sed 's/\n/ /' | sed 's/\$/\\$/g' | sed 's/\[/\\[/g' )
	    a=$( echo ${spec} | cut -d' ' -f1 )
	    b=$( echo ${spec} | cut -d' ' -f2 )
	    uniq=$( comm -12 <( grep -ilr "${a}" | grep ".log" | grep "dsi-outputs" | grep -e "allTests/" -e "${config}/" | sort -u ) <( grep -ilr "${b}" | grep ".log" | grep "dsi-outputs" | grep -e "allTests/" -e "${config}/" | sort -u ) | rev | cut -d/ -f2 | rev | sort -u | wc -l )
	    if [ ${uniq} -eq 1 ]; then
		match=$((${match} + 1))
	    else
		not_match=$((${not_match} + 1))
		echo "output-all-tests-${formatted}.log" >> ${NOT_MATCH_FILE}
	    fi 
	done
    fi
    cd ${SCRIPT_DIR}
}


function main() {
    echo "config,match,not-match" > ${OUT_FILE}
    
    for proj in $( ls ${GENERATED_DATA_DIR} ); do
	[[ "${proj}" == "run-info" ]] && continue
	echo "computing diff for project ${proj}..."
	echo -e "\n#############${proj}#############\n" >> ${NOT_MATCH_FILE}

	compare ${proj} allTests_testClasses
	compare ${proj} allTests_testMethods
    done
}

main
