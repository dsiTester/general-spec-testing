if [ $# -ne 2 ]; then
    echo "USAGE: $0 PROJECT NUM_THREADS"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
REPO_DIR=$( dirname "${SCRIPT_DIR}" )
INSPECTIONS_DIR=${REPO_DIR}/inspections
PROJECT=$1
NUM_THREADS=$2
OUT_DIR=${SCRIPT_DIR}/javert
mkdir -p ${OUT_DIR}
TRACES_DIR=${SCRIPT_DIR}/traces

function prepare() {
    if [ ! -d ${TRACES_DIR} ]; then
        tar xf ${SCRIPT_DIR}/traces.tgz
    fi
    if [ ! -d ${TRACES_DIR}/${PROJECT} ]; then
        (
            cd ${TRACES_DIR}
            tar xf ${PROJECT}.tgz
        )
    fi
}

function main() {
    local project=${PROJECT}
    echo =============================================================${project}
    javert_dir=${OUT_DIR}/${project}
    mkdir -p ${javert_dir}
    cd ${tmpdir}
    (
        export PROJECT
        export javert_dir
        cd ${TRACES_DIR}/${PROJECT}
    ls | grep -v "method-names.txt.gz" | grep -v "processed.txt" | rev | cut -d. -f3- | rev | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/mine-javert-helper.sh
    )
}

prepare
main
