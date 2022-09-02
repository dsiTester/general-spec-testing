# Just a helpful bash script containing functions that would be reused here and there

# rounding scheme
function round() {
    echo $(printf %.$2f $(echo "scale=$2;(((10^$2)*$1)+0.5)/(10^$2)" | bc))
};

# computes and outputs average to target CSV
# CSV must contain a totals column that starts with TOTAL
# arguments: FILE
function compute_avg() {
    local file=$1
    local acc=$( cat ${file} | wc -l | xargs -I {} echo "{} - 2" | bc -l )
    total_str=$( grep ^TOTAL ${file} )
    total_commas="${total_str//[^,]}"
    num_commas=$( echo "${#total_commas} + 1" | bc -l )
    avg_acc_str="AVG"
    for i in $( seq 2 ${num_commas} ); do
        avg=$( echo "${total_str}" | cut -d, -f${i} | xargs -I {} echo "scale=2; {} / ${acc}" | bc -l )
        avg=$( round "${avg}" 1 )
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

