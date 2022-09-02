if [ $# -ne 2 ]; then
    echo "USAGE: $0 PROJECT DSI_ALL_RUNS_OUT"
    exit
fi


SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
DATA_DIR=${BASE_DIR}/data

PROJECT=$1
DSI_ALL_RUNS_OUT=$2

SETUP_FILE=${DATA_DIR}/initial-experiments/${PROJECT}-setup.csv
DSI_OUT=${DSI_ALL_RUNS_OUT}/dsiPlus-dsiAllGranularities-${PROJECT}/${PROJECT}/dsiPlus-dsiAllGranularities/dsi-results
OUT_DIR=${DATA_DIR}/dsi-times-csvs
mkdir -p ${OUT_DIR}
OUT=${OUT_DIR}/${PROJECT}-dsi-times.csv

function get_dsi_time_in_sec() {
    local time=$1
    hr=$( echo "${time}" | cut -d: -f1 )
    min=$( echo "${time}" | cut -d: -f2 )
    sec=$( echo "${time}" | cut -d: -f3 )
    echo "(((${hr} * 60) + ${min}) * 60) + ${sec}" | bc -l
}

function main() {
    echo "id,dsi-time" > ${OUT}
    for id in $( cat ${SETUP_FILE}  | grep ^0 | cut -d, -f1 ); do
        dsi_time=0
        for dsi_file in $( find ${DSI_OUT} -name *${id}.log ); do
            start_time=$( grep --text ^"START TIME" ${dsi_file} | cut -d' ' -f3 )
            start_time_in_sec=$( get_dsi_time_in_sec "${start_time}" )
            end_time=$( grep --text ^"END TIME" ${dsi_file} | cut -d' ' -f3 )
            end_time_in_sec=$( get_dsi_time_in_sec "${end_time}" )
            if (( $(echo "${start_time_in_sec} > ${end_time_in_sec}" | bc -l) )); then # if the run happened over midnight, we need to add a day's worth of seconds
                end_time_in_sec=$( echo "${end_time_in_sec} + 86400" )
            fi
            dsi_time=$( echo "${dsi_time} + (${end_time_in_sec} - ${start_time_in_sec} )" | bc -l)
        done
        echo "${id},${dsi_time}" >> ${OUT}

    done
}

main
