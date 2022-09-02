if [ $# -ne 3 ]; then
    echo "USAGE: $0 PROJECT NUM_LETTERS NUM_THREADS"
    echo "where NUM_LETTERS is 2 for two-letter specs, and NUM_LETTERS is 3 for three-letter specs"
    exit
fi

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
REPO_DIR=$( dirname "${SCRIPT_DIR}" )
INSPECTIONS_DIR=${REPO_DIR}/inspections
PROJECT=$1
NUM_LETTERS=$2
NUM_THREADS=$3
TRACES_DIR=${SCRIPT_DIR}/traces

if [[ "${NUM_LETTERS}" -ne 2 && "${NUM_LETTERS}" -ne 3 ]]; then
    echo "Invalid option for NUM_LETTERS! Exiting."
    exit
fi

OUT_DIR=${SCRIPT_DIR}/bdd-miner-${NUM_LETTERS}
mkdir -p ${OUT_DIR}

function prepare() {
    if [ ! -d ${TRACES_DIR} ]; then
        tar xf ${SCRIPT_DIR}/traces.tgz
    fi
    if [ ! -d ${TRACES_DIR}/${PROJECT} ]; then
        (
            cd ${TRACES_DIR}
            tar xf ${PROJECT}.tgz ${PROJECT}
        )
    fi
}

function main() {
    local project=${PROJECT}
    echo =============================================================${project}
    bdd_dir=${OUT_DIR}/${project}
    mkdir -p ${bdd_dir}
    if [[ "${NUM_LETTERS}" -eq 2 ]]; then
        mkdir -p ${bdd_dir}/ab@q
        mkdir -p ${bdd_dir}/ab@s
        mkdir -p ${bdd_dir}/abp
        mkdir -p ${bdd_dir}/apb
        mkdir -p ${bdd_dir}/abs
        mkdir -p ${bdd_dir}/asb
        mkdir -p ${bdd_dir}/abq@q/
        mkdir -p ${bdd_dir}/aqb@q/
        mkdir -p ${bdd_dir}/abq@s/
        mkdir -p ${bdd_dir}/aqb@s/
        mkdir -p ${bdd_dir}/apbp@q/
        mkdir -p ${bdd_dir}/apbs@q/
        mkdir -p ${bdd_dir}/asbp@q/
        mkdir -p ${bdd_dir}/apbp@s/
        mkdir -p ${bdd_dir}/apbs@s/
        mkdir -p ${bdd_dir}/asbp@s/
    else
        mkdir -p ${bdd_dir}/resource-usages-closed/ # (ab*c)*
        mkdir -p ${bdd_dir}/resource-usages-general/ # (a+b*c+)?
        mkdir -p ${bdd_dir}/resource-usages-general-closed/ # (a+b*c+)*
    fi
    tmpdir=${SCRIPT_DIR}/other-projects-tmp-${project}
    (
        export PROJECT
        export NUM_LETTERS
        export bdd_dir
        cd ${TRACES_DIR}/${project}
        ls | rev | cut -d- -f2- | rev | grep -v ^$ | grep -v ${project}$ | parallel -j ${NUM_THREADS} bash ${SCRIPT_DIR}/mine-bddminer-helper.sh
    )
}

prepare
main
