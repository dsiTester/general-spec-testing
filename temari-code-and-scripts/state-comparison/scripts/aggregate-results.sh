if [ $# -ne 1 ]; then
    echo "USAGE: $0 GENERATED-DATA-DIR"
    exit
fi

GENERATED_DATA_DIR=$1

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
BASE_DIR=$( dirname ${SCRIPT_DIR} )
OUT_DIR=${BASE_DIR}/data/initial-experiments/results
rm -rf ${OUT_DIR}
mkdir -p ${OUT_DIR}

OUT=${OUT_DIR}/all-projects-analysis-summary.csv
>${OUT}
PAPER_OUT_CSV=${OUT_DIR}/state-summary-for-paper.csv
>${PAPER_OUT_CSV}

SETUP_DATA_DIR=${BASE_DIR}/data/initial-experiments/setup-csvs

OG_DSI_OUT_CSV=${OUT_DIR}/original-dsi-accuracy-summary-for-paper.csv
echo "project,total,acc,%acc,hit-ts,hit-ns" > ${OG_DSI_OUT_CSV}

source ${SCRIPT_DIR}/helper.sh

# creating all-projects-analysis-summary.csv
while read entry; do
    if [[ "${entry}" == ^"id"* ]]; then
        echo "${entry},sum" >> ${OUT}
        continue
    elif [[ "${entry}" == *"% accuracy (DSI)"* ]]; then
        total=$( grep "0,Number of total specs (non-nbp)" ${OUT} | cut -d, -f3 )
        tool=$( grep "1,Number of verdict-accurate specs (DSI)" ${OUT} | cut -d, -f3 )
        sum=$( echo "scale=6; (${tool} / ${total}) * 100" | bc -l )
        sum=$( printf "%.2f" "$sum" )
    elif [[ "${entry}" == *"% accuracy (DSI + state)"* ]]; then
        total=$( grep "0,Number of total specs (non-nbp)" ${OUT} | cut -d, -f3 )
        tool=$( grep "2,Number of verdict-accurate specs (DSI + state)" ${OUT} | cut -d, -f3 )
        sum=$( echo "scale=6; (${tool} / ${total}) * 100" | bc -l )
        sum=$( printf "%.2f" "$sum" )
    elif [[ "${entry}" == *"% accuracy (DSI + state + ideal NBP checker / total specs with NBP)"* ]]; then
        total=$( grep "14,Number of total specs (with NBP)" ${OUT} | cut -d, -f3 )
        tool=$( grep "15,Number of verdict-accurate specs (DSI + state + ideal NBP checker)" ${OUT} | cut -d, -f3 )
        sum=$( echo "scale=6; (${tool} / ${total}) * 100" | bc -l )
        sum=$( printf "%.2f" "$sum" )
    elif [[ "${entry}" == *"% hit on True Specs/STS for original DSI"* ]]; then
        total=$( grep -r "true-spec," ${SETUP_DATA_DIR} | wc -l  )
        dsi=$( grep "17,Number of accurate LV specs in original DSI," ${OUT} | cut -d, -f3 )
        sum=$( echo "scale=6; (${dsi} / ${total}) * 100" | bc -l )
        sum=$( printf "%.2f" "$sum" )
    elif [[ "${entry}" == *"% hit on Spurious Specs for original DSI"* ]]; then
        total=$( grep -r "spurious-spec," ${SETUP_DATA_DIR} | wc -l  )
        dsi=$( grep "18,Number of accurate LS specs in original DSI," ${OUT} | cut -d, -f3 )
        sum=$( echo "scale=6; (${dsi} / ${total}) * 100" | bc -l )
        sum=$( printf "%.2f" "$sum" )
    elif [[ "${entry}" == *"% hit on True Specs/STS for DSI + state"* || "${entry}" == *"% hit on Spurious Specs for DSI + state"* ]]; then
        sum="-"
    else
        sum=$( find ${GENERATED_DATA_DIR} -name *-analysis-summary.csv | xargs grep "${entry}" | cut -d, -f3 | paste -sd+ | bc -l )
    fi
    echo "${entry},${sum}" >> ${OUT}
done < <( find ${GENERATED_DATA_DIR} -name *-analysis-summary.csv | grep -v "#" | head -1 | xargs cat | cut -d, -f-2 )

# porting all of the current summaries
find ${GENERATED_DATA_DIR} -name *-analysis-summary.csv | xargs -I {} cp {} ${OUT_DIR}

function output_to_paper_csv() {
    local f=$1
    local proj=$2
    num_dsi_accurate=$( grep "Number of verdict-accurate specs (DSI)" ${f} | rev | cut -d, -f1 | rev )
    num_dsi_state_accurate=$( grep "Number of verdict-accurate specs (DSI + state)" ${f} | rev | cut -d, -f1 | rev )
    num_total=$( grep "Number of total specs (non-nbp)" ${f} | rev | cut -d, -f1 | rev )
    dsi_accuracy_percent=$( grep "% accuracy (DSI)" ${f} | rev | cut -d, -f1 | rev )
    dsi_state_accuracy_percent=$( grep "% accuracy (DSI + state)" ${f} | rev | cut -d, -f1 | rev )
    dsi_ts_hit=$( grep "% hit on True Specs/STS for original DSI" ${f} | rev | cut -d, -f1 | rev )
    dsi_ns_hit=$( grep "% hit on Spurious Specs for original DSI" ${f} | rev | cut -d, -f1 | rev )
    echo "${proj},${num_total},${num_dsi_accurate},${dsi_accuracy_percent},${dsi_ts_hit},${dsi_ns_hit}" >> ${OG_DSI_OUT_CSV}
    echo "${proj},${num_dsi_accurate},${num_dsi_state_accurate},${num_total},${dsi_accuracy_percent},${dsi_state_accuracy_percent}" >> ${PAPER_OUT_CSV}
}


# creating csv for paper
echo "project,DSI-#accurate,DSI+state-#accurate,total,DSI-%accuracy,DSI+state-%accuracy" > ${PAPER_OUT_CSV}
for proj in $( ls ${OUT_DIR} | grep csv | grep -v state-summary | grep -v original-dsi-accuracy-summary | grep -v all-projects | rev | cut -d- -f3- | rev ); do
    f=${OUT_DIR}/${proj}-analysis-summary.csv
    output_to_paper_csv ${f} ${proj}
done
output_to_paper_csv ${OUT} TOTAL
compute_avg ${OG_DSI_OUT_CSV}
