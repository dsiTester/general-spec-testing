if [ $# -ne 2 ]; then
    echo "USAGE: $0 GENERATED-DATA-DIR OUT_DIR"
    echo "ex) OUT_DIR: ${BASE_DIR}/data/initial-experiments/timing-results"
    exit
fi

GENERATED_DATA_DIR=$1
OUT_DIR=$2
mkdir -p ${OUT_DIR}

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )

OUT=${OUT_DIR}/all-projects-timings.csv

source ${SCRIPT_DIR}/helper.sh

# function compute_total_time() {
#     {
#         total_state_comp=0
#         total_state_run=0
#         total_jdk_weave=0
#         total_jdk_compile=0
#         total_jdk_run=0
#         total_total_state=0
#         total_dsi=0
#         while IFS=, read id state_comp state_run jdk_weave jdk_compile jdk_run total_state dsi ; do
#             total_state_comp=$( echo "${total_state_comp} + ${state_comp}" | bc -l )
#             total_state_run=$( echo "${total_state_run} + ${state_run}" | bc -l )
#             total_jdk_weave=$( echo "${total_jdk_weave} + ${jdk_weave}" | bc -l )
#             total_jdk_compile=$( echo "${total_jdk_compile} + ${jdk_compile}" | bc -l )
#             total_jdk_run=$( echo "${total_jdk_run} + ${jdk_run}" | bc -l )
#             total_total_state=$( echo "${total_total_state} + ${total_state}" | bc -l )
#             total_dsi=$( echo "${total_dsi} + ${dsi}" | bc -l )
#         done
#         echo "TOTAL,${total_state_comp},${total_state_run},${total_jdk_weave},${total_jdk_compile},${total_jdk_run},${total_total_state},${total_dsi}" >> ${OUT}
#     } < <( tail -n +2 ${OUT} )
# }

acc=0
for proj in $( ls ${GENERATED_DATA_DIR} ); do
    timings_file=${GENERATED_DATA_DIR}/${proj}/results/${proj}-timings.csv
    if [ "${acc}" -eq 0 ]; then
        echo "$( head -1 ${timings_file}),num-specs" >${OUT}
    fi
    acc=$(( acc + 1 ))
    echo ${proj},$( grep ^"TOTAL," ${timings_file} | cut -d, -f2- ),$( tail -n +2 ${timings_file} | grep -v ^TOTAL | grep -v ^AVG | wc -l ) >> ${OUT}
    cp ${timings_file} ${OUT_DIR}
done

# avg_compile_time=$( echo "scale=2; ${total_compile_time} / ${acc}" | bc -l )
# avg_state_time=$( echo "scale=2; ${total_state_time} / ${acc}" | bc -l )
# avg_dsi_time=$( echo "scale=2; ${total_dsi_time} / ${acc}" | bc -l )
# echo "AVG,${avg_compile_time},${avg_state_time},${avg_dsi_time}"

compute_totals ${OUT}
compute_avg ${OUT}
