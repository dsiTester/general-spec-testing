if [ $# -ne 3 ]; then
    echo "USAGE: bash $0 LOGS_DIR GENERATED_DATA_DIR AUTOMATA_LIST"
    exit
fi

LOGS=$1
GENERATED_DATA_DIR=$2
AUTOMATA_LIST=$3

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
# LOGS=${SCRIPT_DIR}/limit-study-logs
OUT=${BASE_DIR}/data/limit-study/limit-study-times.csv

function compute_avg() {
    local file=$1
    local acc=$( cat ${file} | wc -l | xargs -I {} echo "{} - 2" | bc -l )
    total_str=$( grep ^TOTAL ${file} )
    total_commas="${total_str//[^,]}"
    num_commas=$( echo "${#total_commas} + 1" | bc -l )
    avg_acc_str="AVG"
    for i in $( seq 2 ${num_commas} ); do
        avg=$( echo "${total_str}" | cut -d, -f${i} | xargs -I {} echo "scale=3; {} / ${acc}" | bc -l )
        avg_acc_str=${avg_acc_str},${avg}
    done
    echo ${avg_acc_str}  >> ${file}
}

# argument: name of file
function compute_totals() {
    local file=$1
    labels=$( head -1 ${file} )
    col_commas="${labels//[^,]}"
    num_cols=$( echo "${#col_commas} + 1" | bc -l )
    total_acc_str="TOTAL"
    for i in $( seq 2 ${num_cols} ); do
        total_in_col=$( tail -n +2 ${file} | cut -d, -f${i} | paste -sd+ | bc -l )
        total_acc_str=${total_acc_str},${total_in_col}
    done
    echo ${total_acc_str} >> ${file}
}

function compute_all_stats() {
    local file=$1
    if ! grep -q "TOTAL" ${file}; then
        compute_totals ${file}
        compute_avg ${file}
    fi
}


# computes time from `date +%Y-%m-%d-%H-%M-%S` (ex. 2021-08-23-14-49-45)
function compute_sec_from_date() {
    local time=$1
    date=$( echo ${time} | cut -d'-' -f3 )
    hour=$( echo ${time} | cut -d'-' -f4 )
    min=$( echo ${time} | cut -d'-' -f5 )
    sec=$( echo ${time} | cut -d'-' -f6 )
    time_in_sec=$( echo "((((${date}*24) + ${hour}) * 60) + ${min}) * 60 + ${sec}" | bc -l )
    echo "${time_in_sec}"
}

# computes time from `time` (real) (ex. 4m19.655s)
function compute_sec_from_time() {
    local time=$1
    min=$( echo ${time} | cut -d'm' -f1 );
    sec=$( echo ${time} | cut -d'm' -f2 | cut -d's' -f1 );
    time_in_sec=$( echo "(${min} * 60) + ${sec}" | bc -l )
    echo "${time_in_sec}"
}

function compute_time_in_seconds() {
    local grep_str=$1
    local f=$2
    if ! grep -q "${grep_str}" ${f} ; then
	echo 0
	return
    fi
    start=$( grep "${grep_str} started" ${f} | rev | cut -d' ' -f1 | rev )
    start_in_sec=$( compute_sec_from_date "${start}" )
    end=$( grep "${grep_str} ended" ${f} | rev | cut -d' ' -f1 | rev )
    end_in_sec=$( compute_sec_from_date "${end}" )
    echo "${end_in_sec} - ${start_in_sec}" | bc -l
}

echo "spec,constraint-gen,constraint-filtering,\dsiplus" > ${OUT}
for automaton in $( cat ${AUTOMATA_LIST} ); do # cut -d- -f4-
    echo $automaton
    automaton_log=${LOGS}/gol-*-${automaton}
    dollarless_automaton=$( echo "${automaton}" | sed 's/\$//g' )
    # constraint_gen=$( grep ^real ${CONSTRAINT_GENERATION_LOGS}/gol-${automaton} | cut -d'm' -f2- | cut -d's' -f1 )
    constraint_gen=$( compute_time_in_seconds "Creating constraints" ${GENERATED_DATA_DIR}/${dollarless_automaton}/logs/gol-run-pml )
    if grep -q "EXTRACTING CONSTRAINTS ended" ${automaton_log} ; then # if we made it past the constraints
	constraint_filtering=$( compute_time_in_seconds "EXTRACTING CONSTRAINTS" ${automaton_log} )
    else # then our entire execution is just 
	constraint_filtering=$( compute_sec_from_time $( grep ^real ${automaton_log} | cut -f2 ) )
    fi
    nbp=$( compute_time_in_seconds "RUNNING NBP CHECKER" ${automaton_log} )
    state=$( compute_time_in_seconds "RUNNING STATE CHECKER" ${automaton_log} )
    dsi=$( compute_time_in_seconds "RUNNING DSI" ${automaton_log} )
    dsiplus=$( echo "${nbp} + ${state} + ${dsi}" | bc -l )
    echo "${automaton},${constraint_gen},${constraint_filtering},${dsiplus}" >> ${OUT}
done
compute_all_stats ${OUT}
