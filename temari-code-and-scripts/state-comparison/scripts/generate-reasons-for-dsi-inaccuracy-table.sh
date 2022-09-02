if [ $# -ne 1 ]; then
    echo "USAGE: bash $0 GENERATED-DATA-DIR"
    exit
fi

GENERATED_DATA_DIR=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
SETUP_CSVS_DIR=${DATA_DIR}/initial-experiments/setup-csvs
OUT=${DATA_DIR}/analysis-data/reasons-for-dsi-inaccuracy.csv

WS=${SCRIPT_DIR}/ws
rm -rf ${WS}
mkdir -p ${WS}

# tag_cat_dict =
# {"Return" : {"METHOD_A_RETURNS_VOID", "REPLACE_RETURN_WITH_UNEXPECTED_OUTPUT", "NULL_REPLACEMENT_CAUSED_NULLPOINTEREXCEPTION", "REPLACE_RETURN_WITH_CORRECT_VALUE_NONASSERTION", "REPLACE_RETURN_WITH_EXPECTED_OUTPUT", "DEFAULT_VALUE_SAME_AS_RETURN", "RETURN_VALUE_DISCARDED", "REPLACEMENT_TRIGGERS_CHECK_THAT_PREVENTS_B", "LOSSY_REPLACEMENT"}
# "Relationship" : {"UNRELATED_STATEFUL_METHODS", "ONE_STATELESS_METHOD", "ONE_PURE_SETTER", "CONNECTION_DOES_NOT_NECESSITATE_ORDERING", "UNRELATED_STATELESS_METHODS", "UNRELATED_PURE_SETTERS", "TRUE_SPEC_WITH_CALLER", "MODIFIED_STATE_DOES_NOT_INTERSECT"}
# "Exception": {"EXPECTED_EXCEPTION", "EXPECTED_EXCEPTION_NOT_THROWN", "SWALLOWED_EXCEPTION", "IMPLICITLY_EXPECTED_EXCEPTION", "DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION"}
# "Oracle" : {"WEAK_ORACLE", "ORDER_OF_ASSERTIONS"}
# "Delay": {"STATE_POLLUTION_BY_DSI", "STATE_RESTORED", "CHECKS_MISDIRECTED_OUTPUT", "DELAY_CAUSES_TIMEOUT", "DELAY_CAUSES_OUTPUT_CORRUPTION"}
# "Misc" : {"CONCURRENCY", "DYNAMIC_DISPATCH", "ASM_ERROR", "CONFIGURATION", "DYNAMIC_DISPATCH_SAME_METHOD", "SPECIAL_NBP", "SUPPLEMENTARY_EVIDENCE_BY_JAVADOC", "REVERSE_NBP", "UNINTERESTING_SPEC", "NO_COVERAGE", "MULTIPLE_PERTURBATIONS"}}

# FIXME: need to get that interference stuff. Also is "not share state" based on manual findings?
for project in $( ls ${GENERATED_DATA_DIR} ); do
    state_file=${GENERATED_DATA_DIR}/${project}/results/${project}-results.csv #${SETUP_CSVS_DIR}/${project}-setup.csv
    grep -E "WEAK_ORACLE|ORDER_OF_ASSERTIONS" ${state_file} >> ${WS}/weak.csv
    grep -E "REPLACE_RETURN_WITH_UNEXPECTED_OUTPUT|NULL_REPLACEMENT_CAUSED_NULLPOINTEREXCEPTION|REPLACE_RETURN_WITH_CORRECT_VALUE_NONASSERTION|REPLACE_RETURN_WITH_EXPECTED_OUTPUT|DEFAULT_VALUE_SAME_AS_RETURN|RETURN_VALUE_DISCARDED|REPLACEMENT_TRIGGERS_CHECK_THAT_PREVENTS_B|LOSSY_REPLACEMENT" ${state_file} >> ${WS}/return.csv # METHOD_A_RETURNS_VOID|
    grep -E "EXPECTED_EXCEPTION$|EXPECTED_EXCEPTION;|EXPECTED_EXCEPTION_NOT_THROWN|SWALLOWED_EXCEPTION|IMPLICITLY_EXPECTED_EXCEPTION|DELAY_OF_A_CAUSES_UNEXPECTED_EXCEPTION" ${state_file} >> ${WS}/exceptions.csv
    grep -E "CONCURRENCY|DYNAMIC_DISPATCH|ASM_ERROR|CONFIGURATION|DYNAMIC_DISPATCH_SAME_METHOD|SPECIAL_NBP|SUPPLEMENTARY_EVIDENCE_BY_JAVADOC|REVERSE_NBP|NO_COVERAGE|MULTIPLE_PERTURBATIONS" ${state_file} >> ${WS}/misc.csv
done
echo > ${WS}/interference.csv

echo "category,total,spurious,num-not-share,num-not-share-tool" > ${OUT}
function output() {
    local f=$1
    name=$( echo "${f}" | cut -d. -f1 )
    total=$( cat ${WS}/${f} | wc -l )
    spurious=$( grep "spurious-spec" ${WS}/${f} | wc -l )
    per_spurious=$( echo "scale=6; (${spurious}/${total}) * 100" | bc -l )
    per_spurious=$( printf "%.2f" ${per_spurious} )
    # conservative underapproximation
    num_not_share_state=$( grep -E "UNRELATED_STATEFUL_METHODS|UNRELATED_STATELESS_METHODS|UNRELATED_PURE_SETTERS|ONE_STATELESS_METHOD" ${WS}/${f} | wc -l )
    per_not_share_state=$( echo "scale=6; (${num_not_share_state}/${total}) * 100" | bc -l )
    per_not_share_state=$( printf "%.2f" ${per_not_share_state} )
    num_tool_not_share_state=$( cat ${WS}/${f} | cut -d, -f2 | grep "no" | wc -l )
    per_tool_not_share_state=$( echo "scale=6; (${num_tool_not_share_state}/${total}) * 100" | bc -l )
    per_tool_not_share_state=$( printf "%.2f" ${per_tool_not_share_state} )
    echo "${name},${total},${per_spurious},${per_not_share_state},${per_tool_not_share_state}" >> ${OUT}
}

output weak.csv
output return.csv
output interference.csv
output exceptions.csv
output misc.csv
