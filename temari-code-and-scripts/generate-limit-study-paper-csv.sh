if [ $# -ne 2 ]; then
    echo "USAGE: bash $0 PROCESSED_LIMIT_STUDY MINERS_RANKINGS_DIR"
    exit
fi

PROCESSED_LIMIT_STUDY=$1
MINERS_RANKINGS_DIR=$2
# AUTOMATON_STATS=$2

OUTCOMES=${PROCESSED_LIMIT_STUDY}/summary-results.csv
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data
# AUTOMATON_STATS=${DATA_DIR}/specs-for-limit-study.csv

OUT=${DATA_DIR}/limit-study/limit-study-outcomes.csv
ORDERED_OUT=${DATA_DIR}/limit-study/ordered-limit-study-outcomes.csv
mkdir -p ${DATA_DIR}/limit-study

echo "automaton,states,transitions,letters,num_constraints,num_spurious_constraints,num_nbp_constraints,num_true_constraints,num_unknown_constraints" > ${OUT}
for line in $( cat ${OUTCOMES} | grep -v ^automaton, ); do
    automaton=$( echo "${line}" | cut -d, -f1 )
    miner=$( echo "${automaton}" | cut -d@ -f1 )
    proj=$( echo "${automaton}" | cut -d@ -f2 )
    name=$( echo "${automaton}" | cut -d@ -f3 )
    AUTOMATON_STATS=${MINERS_RANKINGS_DIR}/${miner}-rankings-wo-dollars.csv
    num_constraints=$( echo "${line}" | cut -d, -f6 )
    num_spurious_constraints=$( echo "${line}" | cut -d, -f3 )
    num_nbp_constraints=$( echo "${line}" | cut -d, -f5 )
    num_true_constraints=$( echo "${line}" | cut -d, -f2 )
    num_unknown_constraints=$( echo "${line}" | cut -d, -f4 )
    automaton_for_grep=${proj},${name}
    states=$( grep ^"${automaton_for_grep}", ${AUTOMATON_STATS} | cut -d, -f3 )
    transitions=$( grep ^"${automaton_for_grep}", ${AUTOMATON_STATS} | cut -d, -f4 )
    letters=$( grep ^"${automaton_for_grep}", ${AUTOMATON_STATS} | cut -d, -f5 )
    echo "${automaton},${states},${transitions},${letters},${num_constraints},${num_spurious_constraints},${num_nbp_constraints},${num_true_constraints},${num_unknown_constraints}" >> ${OUT}
done

cat ${OUT} | sort -t, -k2,2 -k3,3 -n > ${ORDERED_OUT}
