if [ $# -ne 3 ]; then
    echo "USAGE: bash $0 WS OUT_DIRECTORY NUM_THREADS"
    exit
fi

WS=$1
OUT=$2
NUM_THREADS=$3
SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
LOGS_DIR=${OUT}/logs
DSM=${SCRIPT_DIR}/DSM

mkdir -p ${LOGS_DIR} # makes both OUT and LOGS_DIR


if [ ! -d ${DSM} ]; then
    (
        cd ${SCRIPT_DIR}
        git clone https://github.com/hvdthong/DSM.git
    )
fi

# creating workspace

echo "run-dsm.sh: Starting parallel run of DSM at `date +%Y-%m-%d-%H-%M-%S`"
(
    export WS
    export DSM
    export OUT
    export LOGS_DIR
    export TRACES_DIR

    ls ${WS} | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/run-dsm-parallel.sh
)
echo "run-dsm.sh: Ending parallel run of DSM at `date +%Y-%m-%d-%H-%M-%S`"
