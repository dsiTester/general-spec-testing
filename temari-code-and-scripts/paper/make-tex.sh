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

bash ${SCRIPT_DIR}/generate-limit-study-timings-tex.sh ${PAPER_DIR}
bash ${SCRIPT_DIR}/generate-limit-study-outcomes-tex.sh ${PAPER_DIR}
