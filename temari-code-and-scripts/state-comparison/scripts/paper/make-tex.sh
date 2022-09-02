if [ $# -ne 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
PAPER_DIR=$1
TABLE_DIR=${PAPER_DIR}/tables
PLOTS_DIR=${PAPER_DIR}/plots
mkdir -p ${TABLE_DIR}
mkdir -p ${PLOTS_DIR}

bash ${SCRIPT_DIR}/generate-state-accuracy-summary-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/generate-original-dsi-accuracy-summary-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/new-generate-original-dsi-accuracy-summary-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/new-generate-updated-dsi-accuracy-summary-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/generate-reasons-for-dsi-inaccuracy-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/generate-state-vs-dsi-timings-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/generate-static-vs-dynamic-tex.sh ${PAPER_DIR}
