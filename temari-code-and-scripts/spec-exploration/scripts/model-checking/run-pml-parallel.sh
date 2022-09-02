if [ $# -lt 2 ]; then
    echo "USAGE: $0 PML_FILE NUM_THREADS SETUP_SPIN_OPT"
    echo "if SETUP_SPIN_OPT is -no, then the script will not build spin"
    exit
fi

IN_FILE=$1
NUM_THREADS=$2
SETUP_SPIN_OPT=$3

IN_DIR=$( dirname ${IN_FILE} )
IN_NAME=$( echo "${IN_FILE}" | rev | cut -d/ -f1 | cut -d. -f2- | rev )

SCRIPT_DIR=$( cd $( dirname $0 ) && pwd )
SPIN_DIR=${SCRIPT_DIR}/spin-tool
DATA_DIR=${SCRIPT_DIR}/data/generated-data
OUT_DIR=${DATA_DIR}/${IN_NAME}

if [ -d ${OUT_DIR} ]; then
    mv ${DATA_DIR} ${DATA_DIR}-`date +%Y-%m-%d-%H-%M-%S`
fi

FILES_DIR=${OUT_DIR}/files
LOGS_DIR=${OUT_DIR}/logs
mkdir -p ${LOGS_DIR}
mkdir -p ${FILES_DIR}

# we're going to work on a copy of the input pml file.
PML_FILE=${FILES_DIR}/input.pml
cp ${IN_FILE} ${PML_FILE}

# copy over the mapping file
cp ${IN_DIR}/${IN_NAME}.mapping ${FILES_DIR}

function setup_spin() {
    echo "[ LOG ] Setting up spin"
    if [ ! -d ${SPIN_DIR} ]; then
        (
            cd ${SCRIPT_DIR}
            git clone git@github.com:nimble-code/Spin.git spin-tool
        )
    fi
    local log_file=${LOGS_DIR}/gol-build-spin
    (
        cd ${SPIN_DIR}
        ( make ) &> ${log_file}
    )
    if grep -q "yacc: Command not found" ${log_file} ; then
        echo -e "[ERROR] yacc is not installed. Please install yacc by running:\n sudo apt-get install bison -y"
        exit
    fi
}

function create_constraints() {
    echo "[ LOG ] Creating constraints started at "`date +%Y-%m-%d-%H-%M-%S`
    local methods_file=${FILES_DIR}/methods.txt
    head -1 ${IN_FILE} | cut -d\{ -f2 | cut -d\} -f1 | tr -d " " | sed 's/,/\n/g' | grep -v EPSILON > ${methods_file}
    (
        cd ${SCRIPT_DIR}
        javac -cp guava-31.1-jre.jar CreateLTLConstraints4.java && java -cp .:guava-31.1-jre.jar CreateLTLConstraints4 ${methods_file} &> ${FILES_DIR}/constraints.txt
    )

    echo "[ LOG ] Creating constraints ended at "`date +%Y-%m-%d-%H-%M-%S`
}

function postprocess() {
    echo "[ LOG ] Postprocessing"
    cat ${LOGS_DIR}/gol-run-spin | grep , | grep -v ^$ | grep -v ^real | grep -v ^user | grep -v ^sys > ${FILES_DIR}/outcomes.txt
}

function main() {
    create_constraints
    echo "[ LOG ] Running spin"
    (
        export PML_FILE
        export SPIN_DIR
        export LOGS_DIR
        export FILES_DIR
        cat ${FILES_DIR}/constraints.txt | parallel -j ${NUM_THREADS} time bash ${SCRIPT_DIR}/run-spin-on-constraints-atomic.sh
    ) &> ${LOGS_DIR}/gol-run-spin
    postprocess
    # bash s.sh fsa.pml
    # ./Src/spin -t fsa.pml # to see counterexamples
    # ./Src/spin fsa.pml # to simulate
    # ./Src/spin -u50 fsa.pml # to simulate within 50 *SPIN* steps
}

if [ "${SETUP_SPIN_OPT}" != "-no" ]; then
    setup_spin
fi
main
