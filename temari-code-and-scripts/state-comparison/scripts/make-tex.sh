if [ $# -ne 1 ]; then
    echo "USAGE: $0 PAPER_DIR"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
SUBSCRIPT_DIR=${SCRIPT_DIR}/tables-for-paper
PAPER_DIR=$1
TABLE_DIR=${PAPER_DIR}/tables
PLOTS_DIR=${PAPER_DIR}/plots
mkdir -p ${TABLE_DIR}
mkdir -p ${PLOTS_DIR}

bash ${SUBSCRIPT_DIR}/make-state-summary.sh ${TABLE_DIR}
