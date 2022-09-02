function compute_avg_time() {
    local acc=$1
    local OUT=$2
    total_str=$( grep ^TOTAL ${OUT} )
    total_commas="${total_str//[^,]}"
    num_commas=$( echo "${#total_commas} + 1" | bc -l )
    avg_acc_str="AVG"
    echo ${num_commas}
    for i in $( seq 2 ${num_commas} ); do
        avg=$( echo "${total_str}" | cut -d, -f${i} | xargs -I {} echo "scale=3; {} / ${acc}" | bc -l )
        avg_acc_str=${avg_acc_str},${avg}
    done
    echo ${avg_acc_str}  >> ${OUT}
}
