if [ $# -ne 2 ]; then
    echo "USAGE: sudo bash $0 NUM_THREADS"
    echo "where NUM_THREADS is the number of cores to be used"
    exit
fi

PROJECT=$1
NUM_THREADS=$2

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
TEXADA_DIR=${SCRIPT_DIR}/texada

cd ${SCRIPT_DIR}

# unpacking traces dir
if [ ! -d ${SCRIPT_DIR}/texada-traces ]; then
    (
	cd ${SCRIPT_DIR}
	tar xf texada-traces
    )
fi

# for compressed in $( ls ${SCRIPT_DIR}/texada-traces | grep tgz ); do
if [ ! -f ${SCRIPT_DIR}/texada-traces/${PROJECT}-texada-traces.txt ]; then
    compressed=${PROJECT}-texada-traces.txt.tgz
    echo "unpacking trace file ${compressed}"
    (
	cd ${SCRIPT_DIR}/texada-traces
	tar xf ${compressed}
    )
fi
# done

if [ ! -d ${TEXADA_DIR} ]; then
    echo "Installing Texada..."
    bash ${SCRIPT_DIR}/setup-texada.sh
fi

if [ ! -f ${TEXADA_DIR}/run-texada-on-proj.sh ]; then
    cp ${SCRIPT_DIR}/run-texada-on-proj.sh ${TEXADA_DIR}
fi

if [ ! -f ${TEXADA_DIR}/run-texada-on-proj-parallel.sh ]; then
    cp ${SCRIPT_DIR}/run-texada-on-proj-parallel.sh ${TEXADA_DIR}
fi

spot_location=$( sudo find / -name "libspot.so.0" | grep -v texada | tail -1 | rev | cut -d/ -f2- | rev )
export LD_LIBRARY_PATH=${spot_location}

cd ${SCRIPT_DIR}/texada

project=${PROJECT}
    echo "=====Running Texada on ${project}====="
    echo "(Logs stored in ${SCRIPT_DIR}/gol-texada-mine-${project})"
    echo "(output stored in ${SCRIPT_DIR}/texada/logs/${project})"
    ( cd ${TEXADA_DIR} ; set -o xtrace ; bash run-texada-on-proj.sh ${project} ${NUM_THREADS} ; set +o xtrace ) &> ${SCRIPT_DIR}/gol-texada-mine-${project}

    echo "=====Computing number of specs within ${project}====="
    echo "Number of Texada Specifications: "$( ls ${SCRIPT_DIR}/texada/logs/${project}/gol-* | grep -v gol-summary$ | xargs cat | grep -v "\[BEGIN\]" | grep -v ^real | grep -v ^sys | grep -v ^user | grep -v ^+ | grep -v " timeout " | grep -v "start time" | grep -v "end time" | wc -l )
