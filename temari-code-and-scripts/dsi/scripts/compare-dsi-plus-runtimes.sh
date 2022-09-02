DIR=$1
# DIR_2=$2

OUT_DIR=dsi-plus-time-comparison
CSV=${OUT_DIR}/comparison-`date +%Y-%m-%d-%H-%M-%S`.csv

mkdir -p ${OUT_DIR}

function compute_time_in_sec() {
    local time=$1
    date=$( echo ${time} | cut -d'-' -f3 )
    hour=$( echo ${time} | cut -d'-' -f4 )
    min=$( echo ${time} | cut -d'-' -f5 )
    sec=$( echo ${time} | cut -d'-' -f6 )
    time_in_sec=$( echo "((((${date}*24) + ${hour}) * 60) + ${min}) * 60 + ${sec}" | bc )
    echo ${time_in_sec}
}

function compute_run_time_in_sec() {
    local file=$1
    start_time=$( cat ${file} | grep "Experiment Started at: " | rev | cut -d' ' -f1 | rev )
    start_time_in_sec=$( compute_time_in_sec ${start_time} )
    end_time=$( cat ${file} | grep "Experiment ended at: " | rev | cut -d' ' -f1 | rev )
    end_time_in_sec=$( compute_time_in_sec ${end_time} )
    echo "${end_time_in_sec} - ${start_time_in_sec} " | bc -l
}

function main() {

    echo "Project-Name,DSI+,LEVEL-1-DSI+,LEVEL-2-DSI+,LEVEL-3-DSI+" > ${CSV}
    echo -e "Comparing times against ${DIR} and ${DIR}...\n"
    for i in $( ls ${DIR}/run-info | rev | cut -d'-' -f8- | rev | sort -u ); do
	[[ $( ls ${DIR}/run-info/${i}* | wc -l ) -lt 3 ]] && continue
	dsi_plus=$( compute_run_time_in_sec ${DIR}/run-info/${i}allGranularities* )
	level_1_file=$( ls ${DIR}/run-info/${i}-* | head -1 )
	level_2_file=$( ls ${DIR}/run-info/${i}-* | head -2 | tail -n +2 )
	level_3_file=$( ls ${DIR}/run-info/${i}-* | head -3 | tail -n +3 )
	level_1=$( compute_run_time_in_sec ${level_1_file}  )
	level_2=$( compute_run_time_in_sec ${level_2_file}  )
	level_3=$( compute_run_time_in_sec ${level_3_file}  )
	echo "${i},${dsi_plus},${level_1},${level_2},${level_3}" >> ${CSV}
    done
    # RUN_INFO_1=
    # RUN_INFO_2=
    # run_time_1=$( compute_run_time_in_sec ${RUN_INFO_1} )
    # run_time_2=$( compute_run_time_in_sec ${RUN_INFO_2} )
    # diff=$( echo "${run_time_2} - ${run_time_1}" | bc -l )
    # echo "${RUN_INFO_2} - ${RUN_INFO_1} is ${diff}"
}

main
